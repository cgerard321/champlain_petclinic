import * as React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { FormEvent, useState, useEffect } from 'react';
import './EditVisit.css';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import { Status } from '@/features/visits/models/Status';
import { getVisit, updateVisit } from '@/features/visits/api/updateVisit.ts';
import { VisitResponseModel } from './VisitResponseModel';

interface ApiError {
  message: string;
}

type VisitType = {
  visitStartDate: Date;
  description: string;
  petId: string;
  practitionerId: string;
  status: Status;
  reminder: boolean; // Added reminder field
  ownerEmail: string; // Added ownerEmail field
};

const formatDate = (date: Date): string => {
  const pad = (n: number): string => n.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const EditingVisit: React.FC = (): JSX.Element => {
  const { visitId } = useParams<{ visitId: string }>();
  const [visit, setVisit] = useState<VisitType>({
    visitStartDate: new Date(),
    description: '',
    petId: '',
    practitionerId: '',
    status: 'UPCOMING' as Status,
    reminder: false, // Set default reminder value
    ownerEmail: '', // Set default ownerEmail value
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
            reminder: response.reminder, // Set reminder value from the backend but keep it non-editable
            ownerEmail: response.ownerEmail, // Populate ownerEmail from backend
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
    if (!visit.ownerEmail) newErrors.ownerEmail = 'Owner Email is required'; // Validate ownerEmail
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
      ...visit,
      visitDate: visit.visitStartDate
        .toISOString()
        .slice(0, 16)
        .replace('T', ' '),
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
      const apiError = error as ApiError;
      setErrorMessage(`Error updating visit: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="profile-edit">
      <h1>Edit Visit</h1>
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
        <label>Practitioner ID: </label>
        <input
          type="text"
          name="practitionerId"
          value={visit.practitionerId}
          onChange={handleChange}
        />
        {errors.practitionerId && (
          <span className="error">{errors.practitionerId}</span>
        )}
        <br />
        <label>Status: </label>
        <select name="status" value={visit.status} onChange={handleChange}>
          <option value="UPCOMING">Upcoming</option>
          <option value="CONFIRMED">Confirmed</option>
          <option value="COMPLETED">Completed</option>
        </select>
        {errors.status && <span className="error">{errors.status}</span>}
        <br />
        <label>Owner Email: </label>
        <input
          type="email"
          name="ownerEmail"
          value={visit.ownerEmail}
          onChange={handleChange}
        />
        {errors.ownerEmail && (
          <span className="error">{errors.ownerEmail}</span>
        )}
        <br />
        {/* Reminder field is not editable but will still be sent in the form */}
        <button type="submit" disabled={isLoading}>
          {isLoading ? 'Updating...' : 'Update'}
        </button>
      </form>
      {showNotification && <div className="notification">{successMessage}</div>}
      {errorMessage && <div className="error">{errorMessage}</div>}
    </div>
  );
};

export default EditingVisit;
