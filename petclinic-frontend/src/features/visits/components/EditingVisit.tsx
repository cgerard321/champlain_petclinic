import * as React from 'react';
<<<<<<< HEAD
=======
import { useNavigate } from 'react-router-dom';
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
import { FormEvent, useState, useEffect } from 'react';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import { Status } from '@/features/visits/models/Status';
import { VisitResponseModel } from '../models/VisitResponseModel';
import { getVisit } from '../api/getVisit';
import { updateVisit } from '../api/updateVisit';
<<<<<<< HEAD
import { getAvailableVets, VetResponse } from '@/features/visits/api/getVets';
=======
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)

import BasicModal from '@/shared/components/BasicModal';

import './EditVisit.css';

interface EditingVisitProps {
  showButton: JSX.Element;
  visitId: string;
}

interface ApiError {
  message: string;
}

type VisitType = {
  visitStartDate: Date;
  description: string;
  petId: string;
<<<<<<< HEAD
  petName: string;
  practitionerId: string;
  isEmergency: boolean;
=======
  practitionerId: string;
  // ownerId: string;
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
  status: Status;
};

const formatDate = (date: Date): string => {
  const pad = (n: number): string => n.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const EditingVisit: React.FC<EditingVisitProps> = ({
  showButton,
  visitId,
}): JSX.Element => {
  const [visit, setVisit] = useState<VisitType>({
    visitStartDate: new Date(),
    description: '',
    petId: '',
<<<<<<< HEAD
    petName: '',
    practitionerId: '',
    isEmergency: false,
=======
    practitionerId: '',
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
    status: 'UPCOMING' as Status,
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

<<<<<<< HEAD
  const [vets, setVets] = useState<VetResponse[]>([]);
=======
  const navigate = useNavigate();
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)

  useEffect(() => {
    const fetchVisitData = async (): Promise<void> => {
      if (visitId) {
        try {
          const response: VisitResponseModel = await getVisit(visitId);
          setVisit({
            practitionerId: response.practitionerId,
            description: response.description,
            petId: response.petId,
<<<<<<< HEAD
            petName: response.petName,
            visitStartDate: new Date(response.visitDate),
            status: response.status,
            isEmergency: response.isEmergency,
=======
            visitStartDate: new Date(response.visitDate),
            status: response.status,
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
          });
        } catch (error) {
          console.error(`Error fetching visit with ID ${visitId}:`, error);
        }
      }
    };

    if (visitId) {
      fetchVisitData();
    }
  }, [visitId]);

<<<<<<< HEAD
  //fetch vets
  useEffect(() => {
    const fetchVets = async (): Promise<void> => {
      try {
        const vetsResponse = await getAvailableVets();
        const activeVets = Array.isArray(vetsResponse)
          ? vetsResponse.filter(vet => vet.active)
          : [];
        setVets(activeVets);
      } catch (error) {
        const apiError = error as ApiError;
        setErrorMessage(`Error loading vets: ${apiError.message}`);
      }
    };

    fetchVets();
  }, []);

=======
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;
    setVisit(prevVisit => ({
      ...prevVisit,
      [name]: name === 'visitStartDate' ? new Date(value) : value, // Convert string to Date object for visitDate
    }));
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!visit.petId) newErrors.petId = 'Pet ID is required';
    if (!visit.visitStartDate)
      newErrors.visitStartDate = 'Visit date is required';
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
<<<<<<< HEAD
    setSuccessMessage('');

    const formattedVisit: VisitRequestModel = {
      visitDate: visit.visitStartDate.toISOString(),
      description: visit.description,
      petId: visit.petId,
      practitionerId: visit.practitionerId,
      isEmergency: visit.isEmergency,
=======
    // setSuccessMessage('');

    const formattedVisit: VisitRequestModel = {
      visitDate: visit.visitStartDate
        .toISOString()
        .slice(0, 16)
        .replace('T', ' '),
      description: visit.description,
      petId: visit.petId,
      practitionerId: visit.practitionerId,
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
      status: visit.status,
    };

    try {
      if (visitId) {
        await updateVisit(visitId, formattedVisit);
        setSuccessMessage('Visit updated successfully!');
        setShowNotification(true);
        setTimeout(() => setShowNotification(false), 3000); // Hide notification after 3 seconds
<<<<<<< HEAD
        setTimeout(() => {
          window.location.reload();
        }, 1000);
=======
        navigate('/visits'); // Navigate to a different page or clear form
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
      }
    } catch (error) {
      // Use type assertion or check error type
      const apiError = error as ApiError;
      setErrorMessage(`Error updating visit: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <BasicModal
      title="Edit Visit"
      showButton={showButton}
      formId="modalform"
      validate={validate}
<<<<<<< HEAD
      // refreshPageOnConfirm={true}
      confirmText={isLoading ? 'Updating...' : 'Update'}
      errorMessage={errorMessage}
    >
      <form id="modalform" onSubmit={handleSubmit}>
        <label>Pet Name:</label>
        <input disabled={true} value={visit.petName}></input>
        <br />
        <label>
          Description:{' '}
          {errors.description && (
            <span className="error">{errors.description}</span>
          )}
=======
      confirmText={isLoading ? 'Updating...' : 'Update'}
    >
      <form id="modalform" onSubmit={handleSubmit}>
        <label>
          Pet ID: {errors.petId && <span className="error">Required</span>}
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
        </label>

        <input
          type="text"
<<<<<<< HEAD
          name="description"
          value={visit.description}
          onChange={handleChange}
          required
        />

        <br />
        <div className="form-group">
          <label htmlFor="practitionerId">Veterinarian Preference:</label>
          <select
            id="practitionerId"
            name="practitionerId"
            value={visit.practitionerId}
            onChange={handleChange}
          >
            {vets.map(vet => (
              <option key={vet.vetId} value={vet.vetId}>
                Dr. {vet.firstName} {vet.lastName}
                {vet.specialties &&
                  vet.specialties.length > 0 &&
                  ` (${vet.specialties.map(s => s.name).join(', ')})`}
              </option>
            ))}
          </select>
        </div>
        <br />
        <label>
          Visit Date:{' '}
          {errors.visitStartDate && (
            <span className="error">{errors.visitDate}</span>
          )}
=======
          name="petId"
          value={visit.petId}
          onChange={handleChange}
        />

        <br />
        <label>
          <span>Visit Date:</span>
          {errors.visitStartDate && <span className="error">Required</span>}
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
        </label>

        <input
          type="datetime-local"
          name="visitStartDate"
          value={formatDate(visit.visitStartDate)}
          onChange={handleChange}
          required
        />
<<<<<<< HEAD
        <br />
        <label>
          Status:{' '}
          {errors.status && <span className="error">{errors.status}</span>}
        </label>

        <select name="status" value={visit.status} onChange={handleChange}>
          <option value="CONFIRMED">Confirmed</option>
          <option value="UPCOMING">Upcoming</option>
          <option value="COMPLETED">Completed</option>
          <option value="CANCELED">Canceled</option>
          <option value="ARCHIVED">Archived</option>
        </select>
        <br />
        <div className="form-group emergency-toggle-group">
          <label htmlFor="isEmergency">
            <span className="emergency-label-text">
              <span>Emergency Visit</span>
            </span>
            <div className="switch-wrapper">
              <input
                type="checkbox"
                id="isEmergency"
                name="isEmergency"
                checked={visit.isEmergency}
                onChange={e =>
                  setVisit(prev => ({
                    ...prev,
                    isEmergency: e.target.checked,
                  }))
                }
                className="switch-input"
              />
              <span className="switch-slider"></span>
            </div>
          </label>
        </div>
      </form>
      {showNotification && <div className="notification">{successMessage}</div>}
=======

        <br />
        <label>Description: </label>
        {errors.description && (
          <span className="error">{errors.description}</span>
        )}
        <input
          type="text"
          name="description"
          value={visit.description}
          onChange={handleChange}
        />

        <br />
        <label>Practitioner ID: </label>
        {errors.practitionerId && (
          <span className="error">{errors.practitionerId}</span>
        )}
        <input
          type="text"
          name="practitionerId"
          value={visit.practitionerId}
          onChange={handleChange}
        />

        <br />
        <label>Status: </label>
        {errors.status && <span className="error">{errors.status}</span>}
        <select name="status" value={visit.status} onChange={handleChange}>
          <option value="UPCOMING">Upcoming</option>
        </select>
        <br />
      </form>
      {/* {showNotification && <div className="notification">{successMessage}</div>} */}
>>>>>>> a50514b4 (Replaced View and Edit pages with modals for visits)
    </BasicModal>
  );
};

export default EditingVisit;
