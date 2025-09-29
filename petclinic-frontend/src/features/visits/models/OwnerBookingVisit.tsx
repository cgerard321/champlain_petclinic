import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { FormEvent, useState, useEffect } from 'react';
import { useUser } from '@/context/UserContext';
import './EditVisit.css';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import { Status } from '@/features/visits/models/Status';
import { addVisit } from '@/features/visits/api/addVisit';

interface ApiError {
  message: string;
}

type OwnerVisitType = {
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

const OwnerBookingVisit: React.FC = (): JSX.Element => {
  const { user } = useUser();
  const [visit, setVisit] = useState<OwnerVisitType>({
    visitStartDate: new Date(),
    description: '',
    petId: '',
    practitionerId: '',
    status: 'UPCOMING' as Status,
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
        const response = await fetch('http://localhost:8080/api/gateway/vets');
        const vets = await response.json();
        setAvailableVets(Array.isArray(vets) ? vets : [vets]);
      } catch (error) {
        console.error('Error fetching vets:', error);
      }
    };
    fetchVets();
  }, []);

  const formatWorkHours = (hoursArray: string[]): string => {
    if (!hoursArray || hoursArray.length === 0) return 'No hours available';
    return hoursArray
      .map(hour => {
        const parts = hour.split('_');
        if (parts.length === 3) {
          return `${parts[1]}:00-${parts[2]}:00`;
        }
        return hour;
      })
      .join(', ');
  };

  const validateVetAvailability = (
    selectedDate: Date,
    vetInfo: VetInfo | null
  ): { isValid: boolean; message: string } => {
    if (!vetInfo)
      return { isValid: false, message: 'Please select a veterinarian' };

    const dayNames = [
      'Sunday',
      'Monday',
      'Tuesday',
      'Wednesday',
      'Thursday',
      'Friday',
      'Saturday',
    ];
    const selectedDay = dayNames[selectedDate.getDay()];

    if (!vetInfo.workday.includes(selectedDay)) {
      return {
        isValid: false,
        message: `Dr. ${vetInfo.firstName} ${vetInfo.lastName} is not available on ${selectedDay}. Available days: ${vetInfo.workday.join(', ')}`,
      };
    }

    const selectedHour = selectedDate.getHours();

    try {
      const workHours = JSON.parse(vetInfo.workHoursJson);
      const dayHours = workHours[selectedDay];

      if (!dayHours || dayHours.length === 0) {
        return {
          isValid: false,
          message: `No available hours on ${selectedDay}`,
        };
      }

      const isHourAvailable = dayHours.some((hourSlot: string) => {
        const parts = hourSlot.split('_');
        return parts.length === 3 && parseInt(parts[1]) === selectedHour;
      });

      if (!isHourAvailable) {
        const availableHours = dayHours
          .map((slot: string) => {
            const parts = slot.split('_');
            return parts.length === 3 ? `${parts[1]}:00` : slot;
          })
          .join(', ');

        return {
          isValid: false,
          message: `${selectedHour}:00 is not available. Available hours on ${selectedDay}: ${availableHours}`,
        };
      }

      return { isValid: true, message: '' };
    } catch (e) {
      return { isValid: false, message: 'Error checking availability' };
    }
  };

  const handleVetSelection = (vetId: string): void => {
    const selectedVet = availableVets.find(vet => vet.vetId === vetId);
    setSelectedVetInfo(selectedVet || null);
    setShowAvailability(!!selectedVet);
    setVisit(prevVisit => ({ ...prevVisit, practitionerId: vetId }));
  };

  const formatDate = (date: Date): string => {
    const pad = (n: number): string => n.toString().padStart(2, '0');
    const hourOnlyDate = new Date(date);
    hourOnlyDate.setMinutes(0);
    hourOnlyDate.setSeconds(0);
    return `${hourOnlyDate.getFullYear()}-${pad(hourOnlyDate.getMonth() + 1)}-${pad(hourOnlyDate.getDate())}T${pad(hourOnlyDate.getHours())}:00`;
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;

    if (name === 'practitionerId') {
      handleVetSelection(value);
    } else if (name === 'visitStartDate') {
      const selectedDate = new Date(value);
      selectedDate.setMinutes(0);
      selectedDate.setSeconds(0);
      setVisit(prevVisit => ({ ...prevVisit, visitStartDate: selectedDate }));
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
    if (!visit.visitStartDate)
      newErrors.visitStartDate = 'Visit date is required';
    if (!visit.description.trim())
      newErrors.description = 'Description is required';
    if (!visit.practitionerId)
      newErrors.practitionerId = 'Practitioner ID is required';
    if (!visit.status) newErrors.status = 'Status is required';

    if (visit.practitionerId && visit.visitStartDate && selectedVetInfo) {
      const availabilityCheck = validateVetAvailability(
        visit.visitStartDate,
        selectedVetInfo
      );
      if (!availabilityCheck.isValid) {
        newErrors.availability = availabilityCheck.message;
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleCancel = (): void => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/visits');
    }
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
      ownerId: user.userId,
      jwtToken:
        localStorage.getItem('authToken') ||
        localStorage.getItem('token') ||
        '',
      description: visit.description,
      petId: visit.petId,
      practitionerId: visit.practitionerId,
      status: visit.status,
    };

    try {
      await addVisit(formattedVisit);
      setSuccessMessage('Visit added successfully!');
      setShowNotification(true);
      setTimeout(() => setShowNotification(false), 5000);
      navigate('/customer/visits');
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error adding visits: ${apiError.message}`);
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
          <div className="vet-availability-card">
            <h4 className="vet-availability-header">
              <span className="vet-icon">🩺</span>
              Dr. {selectedVetInfo.firstName} {selectedVetInfo.lastName} -
              Availability
            </h4>

            <div className="availability-days-section">
              <strong className="section-label">Available Days:</strong>
              <span className="days-value">
                {selectedVetInfo.workday.join(', ')}
              </span>
            </div>
            <div className="availability-hours-section">
              <strong className="section-label">Available Hours:</strong>
              <div className="hours-container"></div>
              {(() => {
                try {
                  const workHours = JSON.parse(selectedVetInfo.workHoursJson);
                  return Object.entries(workHours).map(([day, hours]) => (
                    <div key={day} className="day-hours-row">
                      <strong className="day-name">{day}:</strong>
                      <span className="hours-list">
                        {formatWorkHours(hours as string[])}
                      </span>
                    </div>
                  ));
                } catch (e) {
                  return <div>Work hours unavailable</div>;
                }
              })()}
            </div>
          </div>
        )}

        {errors.availability && (
          <div className="availability-error">{errors.availability}</div>
        )}

        <label>Status: </label>
        <select name="status" value={visit.status} onChange={handleChange}>
          <option value="UPCOMING">Upcoming</option>
        </select>
        {errors.status && <span className="error">{errors.status}</span>}

        <button className="cancel" type="button" onClick={handleCancel}>
          Cancel
        </button>

        <button type="submit" disabled={isLoading}>
          {isLoading ? 'Adding...' : 'Add'}
        </button>
      </form>
      {showNotification && <div className="notification">{successMessage}</div>}
      {errorMessage && <div className="error">{errorMessage}</div>}
    </div>
  );
};

export default OwnerBookingVisit;
