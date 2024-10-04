import * as React from 'react';
import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { addEmergency } from './Api/addEmergency';
import { EmergencyRequestDTO } from './Model/EmergencyRequestDTO';
import { UrgencyLevel } from './Model/UrgencyLevel';
import './AddEmergencyForm.css';

// Define an interface for the error if known
interface ApiError {
  message: string;
}

const AddEmergencyForm: React.FC = (): JSX.Element => {
  const [emergency, setEmergency] = useState<EmergencyRequestDTO>({
    visitDate: new Date(),
    description: '',
    petName: '',
    urgencyLevel: UrgencyLevel.LOW, // Default urgency level
    emergencyType: '',
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const navigate = useNavigate();

  const handleInputChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ): void => {
    const { name, value } = e.target;
    setEmergency(prevEmergency => ({
      ...prevEmergency,
      [name as keyof EmergencyRequestDTO]: value,
    }));
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    setEmergency(prevEmergency => ({
      ...prevEmergency,
      visitDate: new Date(e.target.value),
    }));
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!emergency.description)
      newErrors.description = 'Description is required';
    if (!emergency.petName) newErrors.petName = 'Pet name is required';
    if (!emergency.emergencyType)
      newErrors.emergencyType = 'Emergency type is required';
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

    try {
      await addEmergency(emergency);
      setSuccessMessage('Emergency added successfully!');
      setShowNotification(true);
      setTimeout(() => setShowNotification(false), 3000); // Hide notification after 3 seconds
      navigate('/visits'); // Navigate to a different page or clear form
      setEmergency({
        visitDate: new Date(),
        description: '',
        petName: '',
        urgencyLevel: UrgencyLevel.LOW,
        emergencyType: '',
      });
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error adding emergency: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="add-emergency-form">
      <h2>Add an Emergency</h2>
      {isLoading && <div className="loader">Loading...</div>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Pet Name:</label>
          <input
            type="text"
            name="petName"
            value={emergency.petName}
            onChange={handleInputChange}
            required
          />
          {errors.petName && <span className="error">{errors.petName}</span>}
        </div>
        <div>
          <label>Description:</label>
          <textarea
            name="description"
            value={emergency.description}
            onChange={handleInputChange}
            required
          />
          {errors.description && (
            <span className="error">{errors.description}</span>
          )}
        </div>
        <div>
          <label>Emergency Type:</label>
          <input
            type="text"
            name="emergencyType"
            value={emergency.emergencyType}
            onChange={handleInputChange}
            required
          />
          {errors.emergencyType && (
            <span className="error">{errors.emergencyType}</span>
          )}
        </div>
        <div>
          <label>Urgency Level:</label>
          <select
            name="urgencyLevel"
            value={emergency.urgencyLevel}
            onChange={handleInputChange}
          >
            <option value={UrgencyLevel.LOW}>Low</option>
            <option value={UrgencyLevel.MEDIUM}>Medium</option>
            <option value={UrgencyLevel.HIGH}>High</option>
          </select>
        </div>
        <div>
          <label>Date:</label>
          <input
            type="date"
            name="visitDate"
            value={emergency.visitDate.toISOString().split('T')[0]}
            onChange={handleDateChange}
            required
          />
        </div>
        <button type="submit">Submit Emergency</button>
      </form>

      {successMessage && <p className="success-message">{successMessage}</p>}
      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {showNotification && (
        <div className="notification">Emergency added successfully!</div>
      )}
    </div>
  );
};

export default AddEmergencyForm;
