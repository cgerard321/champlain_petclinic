import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { FormEvent, useState, useEffect } from 'react';
import './EditVisit.css';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import { Status } from '@/features/visits/models/Status';
import { addVisit } from '@/features/visits/api/addVisit';

interface ApiError {
  message: string;
}

type VisitType = {
  visitStartDate: Date;
  description: string;
  petId: string;
  practitionerId: string;
  status: Status;
};

type VetInfo = {
  vetId: string;
  firstName: string;
  lastName: string;
  workday: string[];
  workHoursJson: string;
};

const AddingVisit: React.FC = (): JSX.Element => {
  const [visit, setVisit] = useState<VisitType>({
    visitStartDate: new Date(),
    description: '',
    petId: '',
    practitionerId: '',
    status: 'UPCOMING' as Status,
    //visitEndDate: new Date(),
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);
  const [availableVets, setAvailableVets] = useState<VetInfo[]>([]);
  const [selectedVetInfo, setSelectedVetInfo] = useState<VetInfo | null>(null);
  const [showAvailability, setShowAvailability] = useState<boolean>(false);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchVets = async (): Promise<void> => {
      try {
        const response = await fetch(
          'http://localhost:8080/api/v2/gateway/vets'
        );
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('text/plain')) {
          // Handle Server-Sent Events format
          const text = await response.text();
          const lines = text
            .split('\n')
            .filter(line => line.startsWith('data:'));
          const vets = lines.map(line => {
            const jsonStr = line.replace('data:', '');
            return JSON.parse(jsonStr);
          });
          setAvailableVets(vets);
        } else {
          // Handle regular JSON
          const vets = await response.json();
          setAvailableVets(Array.isArray(vets) ? vets : [vets]);
        }
      } catch (error) {
        console.error('Error fetching vets:', error);
        try {
          const fallbackResponse = await fetch(
            'http://localhost:8080/api/gateway/vets'
          );
          const vets = await fallbackResponse.json();
          setAvailableVets(Array.isArray(vets) ? vets : [vets]);
        } catch (fallbackError) {
          console.error('Fallback fetch also failed:', fallbackError);
          setAvailableVets([]);
        }
      }
    };

    fetchVets();
  }, []);

  const formatWorkHours = (hoursArray: string[]): string => {
    if (!hoursArray || hoursArray.length === 0) {
      return 'No hours available';
    }

    return hoursArray
      .map(hour => {
        const parts = hour.split('_');
        if (parts.length === 3) {
          const startHour = parseInt(parts[1]);
          const endHour = parseInt(parts[2]);
          return `${startHour}:00-${endHour}:00`;
        }
        return hour;
      })
      .join(', ');
  };

  const handleVetSelection = (vetId: string): void => {
    const selectedVet = availableVets.find(vet => vet.vetId === vetId);
    setSelectedVetInfo(selectedVet || null);
    setShowAvailability(!!selectedVet);

    setVisit(prevVisit => ({
      ...prevVisit,
      practitionerId: vetId,
    }));
  };

  const formatDate = (date: Date): string => {
    const pad = (n: number): string => n.toString().padStart(2, '0');
    const hourOnlyDate = new Date(date);
    hourOnlyDate.setMinutes(0);
    hourOnlyDate.setSeconds(0);
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
      date.getDate()
    )}T${pad(hourOnlyDate.getHours())}:00`;
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;

    if (name === 'practitionerId') {
      handleVetSelection(value);
    } else {
      setVisit(prevVisit => ({
        ...prevVisit,
        [name]: name === 'visitStartDate' ? new Date(value) : value,
      }));
    }
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!visit.petId) newErrors.petId = 'Pet ID is required';
    if (!visit.visitStartDate) newErrors.visitDate = 'Visit date is required';
    if (!visit.description) newErrors.description = 'Description is required';
    if (!visit.practitionerId)
      newErrors.practitionerId = 'Practitioner ID is required';
    if (!visit.status) newErrors.status = 'Status is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    const formattedVisit: VisitRequestModel = {
      visitDate: visit.visitStartDate
        .toISOString()
        .slice(0, 16)
        .replace('T', ' '),
      description: visit.description,
      petId: visit.petId,
      practitionerId: visit.practitionerId,
      status: visit.status,
    };

    try {
      await addVisit(formattedVisit);
      setSuccessMessage('Visit added successfully!');
      setShowNotification(true);
      setTimeout(() => setShowNotification(false), 3000);
      navigate('/visits');
      setVisit({
        visitStartDate: new Date(),
        description: '',
        petId: '',
        practitionerId: '',
        status: 'UPCOMING' as Status,
        //visitEndDate: new Date(),
      });

      setShowAvailability(false);
      setSelectedVetInfo(null);
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error adding visit: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="profile-edit">
      <h1>Schedule Visit For Your Pet</h1>
      <form onSubmit={handleSubmit}>
        <label>Pet ID: </label>
        <input
          type="text"
          name="petId"
          value={visit.petId}
          onChange={handleChange}
        />
        {errors.petId && <span className="error">{errors.petId}</span>}
        <br />
        <label>Visit Date: </label>
        <input
          type="datetime-local"
          name="visitStartDate"
          value={formatDate(visit.visitStartDate)}
          onChange={handleChange}
          required
          step="3600"
          placeholder="Select date and time"
        />
        {errors.visitStartDate && (
          <span className="error">{errors.visitStartDate}</span>
        )}
        <br />

        <label>Description: </label>
        <input
          type="text"
          name="description"
          value={visit.description}
          onChange={handleChange}
        />
        {errors.description && (
          <span className="error">{errors.description}</span>
        )}
        <br />

        <label>Practitioner: </label>
        <select
          name="practitionerId"
          value={visit.practitionerId}
          onChange={handleChange}
        >
          <option value="">Select a Veterinarian</option>
          {availableVets.map(vet => (
            <option key={vet.vetId} value={vet.vetId}>
              Dr. {vet.firstName} {vet.lastName}
            </option>
          ))}
        </select>
        {errors.practitionerId && (
          <span className="error">{errors.practitionerId}</span>
        )}
        <br />

        {showAvailability && selectedVetInfo && (
          <div className="vet-availability-section">
            <h4 className="vet-availability-title">
              Dr. {selectedVetInfo.firstName} {selectedVetInfo.lastName} -
              Availability
            </h4>

            <div className="availability-info">
              <strong>Available Days:</strong>
              <span className="availability-value">
                {selectedVetInfo.workday && selectedVetInfo.workday.length > 0
                  ? selectedVetInfo.workday.join(', ')
                  : 'No workdays specified'}
              </span>
            </div>

            <div>
              <strong>Available Hours:</strong>
              <div className="work-hours-display">
                {selectedVetInfo.workHoursJson ? (
                  (() => {
                    try {
                      const workHours = JSON.parse(
                        selectedVetInfo.workHoursJson
                      );
                      return Object.entries(workHours).map(([day, hours]) => (
                        <div key={day} className="work-hours-day">
                          <strong>{day}:</strong>{' '}
                          {formatWorkHours(hours as string[])}
                        </div>
                      ));
                    } catch (e) {
                      return (
                        <div className="work-hours-error">
                          Work hours format error
                        </div>
                      );
                    }
                  })()
                ) : (
                  <div>No work hours specified</div>
                )}
              </div>
            </div>
          </div>
        )}

        <label>Status: </label>
        <select name="status" value={visit.status} onChange={handleChange}>
          <option value="UPCOMING">Upcoming</option>
        </select>
        {errors.status && <span className="error">{errors.status}</span>}
        <br />
        <button type="submit" disabled={isLoading}>
          {isLoading ? 'Adding...' : 'Add'}
        </button>
      </form>
      {showNotification && <div className="notification">{successMessage}</div>}
      {errorMessage && <div className="error">{errorMessage}</div>}
    </div>
  );
};

export default AddingVisit;
