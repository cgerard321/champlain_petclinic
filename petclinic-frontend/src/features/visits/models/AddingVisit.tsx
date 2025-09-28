import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { FormEvent, useState } from 'react';
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

const AddingVisit: React.FC = (): JSX.Element => {
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
      [name]: name === 'visitStartDate' ? new Date(value) : value, // Convert string to Date object for visitDate
    }));
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

  const handleCancel = (): void => {
    navigate(-1);
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
      await addVisit(formattedVisit); // Pass the Date object directly
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
        </select>
        {errors.status && <span className="error">{errors.status}</span>}
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

export default AddingVisit;
