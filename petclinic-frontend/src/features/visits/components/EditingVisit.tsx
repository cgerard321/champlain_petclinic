import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { FormEvent, useState, useEffect } from 'react';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import { Status } from '@/features/visits/models/Status';
import { VisitResponseModel } from '../models/VisitResponseModel';
import { getVisit } from '../api/getVisit';
import { updateVisit } from '../api/updateVisit';

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
  practitionerId: string;
  // ownerId: string;
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
    practitionerId: '',
    status: 'UPCOMING' as Status,
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchVisitData = async (): Promise<void> => {
      if (visitId) {
        try {
          const response: VisitResponseModel = await getVisit(visitId);
          setVisit({
            practitionerId: response.practitionerId,
            description: response.description,
            petId: response.petId,
            visitStartDate: new Date(response.visitDate),
            status: response.status,
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
      if (visitId) {
        await updateVisit(visitId, formattedVisit);
        setSuccessMessage('Visit updated successfully!');
        setShowNotification(true);
        setTimeout(() => setShowNotification(false), 3000); // Hide notification after 3 seconds
        navigate('/visits'); // Navigate to a different page or clear form
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
      refreshPageOnConfirm={true}
      confirmText={isLoading ? 'Updating...' : 'Update'}
      errorMessage={errorMessage}
    >
      <form id="modalform" onSubmit={handleSubmit}>
        <label>
          Pet ID: {errors.petId && <span className="error">Required</span>}
        </label>

        <input
          type="text"
          name="petId"
          value={visit.petId}
          onChange={handleChange}
          required
        />

        <br />
        <label>
          Visit Date:{' '}
          {errors.visitStartDate && <span className="error">Required</span>}
        </label>

        <input
          type="datetime-local"
          name="visitStartDate"
          value={formatDate(visit.visitStartDate)}
          onChange={handleChange}
          required
        />

        <br />
        <label>
          Description:{' '}
          {errors.description && (
            <span className="error">{errors.description}</span>
          )}
        </label>

        <input
          type="text"
          name="description"
          value={visit.description}
          onChange={handleChange}
          required
        />

        <br />
        <label>
          Practitioner ID:{' '}
          {errors.practitionerId && (
            <span className="error">{errors.practitionerId}</span>
          )}
        </label>

        <input
          type="text"
          name="practitionerId"
          value={visit.practitionerId}
          onChange={handleChange}
          required
        />

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
      </form>
      {showNotification && <div className="notification">{successMessage}</div>}
    </BasicModal>
  );
};

export default EditingVisit;
