import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { FormEvent, useState } from 'react';
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
  isEmergency: boolean;
};

const OwnerBookingVisit: React.FC = (): JSX.Element => {
  const { user } = useUser();
  const [visit, setVisit] = useState<OwnerVisitType>({
    visitStartDate: new Date(),
    description: '',
    petId: '',
    practitionerId: '',
    status: 'UPCOMING' as Status,
    isEmergency: false,
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const navigate = useNavigate();

  const formatDate = (date: Date): string => {
    const pad = (n: number): string => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;
    setVisit(prevVisit => ({
      ...prevVisit,
      [name]: name === 'visitStartDate' ? new Date(value) : value,
    }));
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
      isEmergency: visit.isEmergency,
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
