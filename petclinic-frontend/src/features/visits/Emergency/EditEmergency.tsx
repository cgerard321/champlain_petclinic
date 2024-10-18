/*import { useNavigate, useParams } from 'react-router-dom';
import * as React from 'react';
import { FormEvent, useState, useEffect } from 'react';
import { EmergencyRequestDTO } from './Model/EmergencyRequestDTO';
import { EmergencyResponseDTO } from './Model/EmergencyResponseDTO';
import { getEmergency, updateEmergency } from './Api/updateEmegency';
import { UrgencyLevel } from './Model/UrgencyLevel';
import './AddEmergencyForm.css';

interface ApiError {
  message: string;
}

const EditEmergency: React.FC = (): JSX.Element => {
  const { emergencyId } = useParams<{ emergencyId: string }>();
  const [emergency, setEmergency] = useState<EmergencyRequestDTO | null>(null);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isDataLoading, setIsDataLoading] = useState<boolean>(true);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchEmergencyData = async (): Promise<void> => {
      if (emergencyId) {
        try {
          setIsDataLoading(true);

          const response: EmergencyResponseDTO =
            await getEmergency(emergencyId);

          setEmergency({
            visitDate: new Date(response.visitDate),
            description: response.description,
            petName: response.petName,
            urgencyLevel: response.urgencyLevel,
            emergencyType: response.emergencyType,
          });
          setIsDataLoading(false);
        } catch (error) {
          console.error(
            `Error fetching emergency with ID ${emergencyId}:`,
            error
          );
          setIsDataLoading(false);
        }
      } else {
        console.error('No emergencyId found.');
        setIsDataLoading(false);
      }
    };

    fetchEmergencyData().catch(error =>
      console.error('Error in fetchEmergencyData: ', error)
    );
  }, [emergencyId]);

  const handleInputChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ): void => {
    const { name, value } = e.target;
    setEmergency(prevEmergency => ({
      ...prevEmergency!,
      [name as keyof EmergencyRequestDTO]: value,
    }));
  };

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    setEmergency(prevEmergency => ({
      ...prevEmergency!,
      visitDate: new Date(e.target.value),
    }));
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!emergency?.description)
      newErrors.description = 'Description is required';
    if (!emergency?.petName) newErrors.petName = 'Pet name is required';
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
      if (emergencyId && emergency) {
        await updateEmergency(emergencyId, emergency);
        setSuccessMessage('Emergency updated successfully!');
        setShowNotification(true);
        setTimeout(() => setShowNotification(false), 3000);
        navigate('/visits');
      }
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error updating emergency: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  if (isDataLoading) {
    return <div>Loading emergency data...</div>;
  }

  return (
    <div className="add-emergency-form">
      <h2>Edit Emergency</h2>
      {isLoading && <div className="loader">Loading...</div>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Visit Date:</label>
          <input
            type="date"
            name="visitDate"
            value={emergency?.visitDate.toISOString().split('T')[0]}
            onChange={handleDateChange}
            required
          />
        </div>
        <div>
          <label>Description:</label>
          <textarea
            name="description"
            value={emergency?.description || ''}
            onChange={handleInputChange}
            required
          />
          {errors.description && (
            <span className="error">{errors.description}</span>
          )}
        </div>
        <div>
          <label>Pet Name:</label>
          <input
            type="text"
            name="petName"
            value={emergency?.petName || ''}
            onChange={handleInputChange}
            required
          />
          {errors.petName && <span className="error">{errors.petName}</span>}
        </div>
        <div>
          <label>Urgency Level:</label>
          <select
            name="urgencyLevel"
            value={emergency?.urgencyLevel || UrgencyLevel.LOW}
            onChange={handleInputChange}
            required
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
        </div>
        <div>
          <label>Emergency Type:</label>
          <input
            type="text"
            name="emergencyType"
            value={emergency?.emergencyType || ''}
            onChange={handleInputChange}
            required
          />
        </div>
        <button type="submit">Update Emergency</button>
      </form>

      {successMessage && <p className="success-message">{successMessage}</p>}
      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {showNotification && (
        <div className="notification">Emergency updated successfully!</div>
      )}
    </div>
  );
};

export default EditEmergency;
*/
