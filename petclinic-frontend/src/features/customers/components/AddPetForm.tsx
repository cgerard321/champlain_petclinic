import * as React from 'react';
import { FormEvent, useState } from 'react';
import { addPetForOwner } from '../api/addPetForOwner';
import { PetRequestModel } from '../models/PetRequestModel';
import { useNavigate, useParams } from 'react-router-dom';
import './customers.css';

const petTypeOptions: { [key: string]: string } = {
  '1': 'Cat',
  '2': 'Dog',
  '3': 'Lizard',
  '4': 'Snake',
  '5': 'Bird',
  '6': 'Hamster',
};

const AddPetForm: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const { ownerId } = useParams<{ ownerId: string }>();
  const [pet, setPet] = useState<PetRequestModel>({
    ownerId: ownerId || '',
    name: '',
    birthDate: new Date(),
    petTypeId: '',
    isActive: 'true',
    weight: '',
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, type, value } = e.target;
    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setPet({
        ...pet,
        [name]: checked ? 'true' : 'false',
      });
    } else if (type === 'date') {
      setPet({
        ...pet,
        [name]: new Date(value),
      });
    } else {
      setPet({
        ...pet,
        [name]: value,
      });
    }
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!pet?.name) newErrors.name = 'Name is required';
    if (!pet?.weight) newErrors.weight = 'Weight is required';
    if (!pet?.petTypeId) newErrors.petTypeId = 'Pet Type is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate() || !ownerId) return;
    try {
      const response = await addPetForOwner(ownerId, pet);
      if (response.status === 201) {
        setSuccessMessage('Pet added successfully!');
        setIsAddModalOpen(true);
      } else {
        console.error('Error adding pet');
      }
    } catch (error) {
      console.error('Error adding pet:', error);
    }
  };

  const closeAddModal = (): void => {
    setIsAddModalOpen(false);
    navigate(`/customers/${ownerId}`);
  };

  return (
    <div className="form-container">
      <h1>Add New Pet</h1>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Name</label>
          <input
            type="text"
            name="name"
            value={pet.name}
            onChange={handleChange}
            className={errors.name ? 'error-input' : ''}
          />
          {errors.name && <span className="error-message">{errors.name}</span>}
        </div>

        <div className="form-group">
          <label>Pet Type</label>
          <select
            name="petTypeId"
            value={pet.petTypeId}
            onChange={handleChange}
            className={errors.petTypeId ? 'error-input' : ''}
          >
            <option value="">Select a pet type</option>
            {Object.entries(petTypeOptions).map(([id, name]) => (
              <option key={id} value={id}>
                {name}
              </option>
            ))}
          </select>
          {errors.petTypeId && (
            <span className="error-message">{errors.petTypeId}</span>
          )}
        </div>

        <div className="form-group">
          <label>Birth Date</label>
          <input
            type="date"
            name="birthDate"
            value={pet.birthDate.toISOString().split('T')[0]}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label>Weight (kg)</label>
          <input
            type="text"
            name="weight"
            value={pet.weight}
            onChange={handleChange}
            className={errors.weight ? 'error-input' : ''}
          />
          {errors.weight && (
            <span className="error-message">{errors.weight}</span>
          )}
        </div>

        <div className="form-group" style={{ textAlign: 'center' }}>
          <button type="submit" className="button-base primary-button">
            Add Pet
          </button>
          <button
            type="button"
            onClick={() => navigate(`/customers/${ownerId}`)}
            className="button-base secondary-button mt-2"
          >
            Cancel
          </button>
        </div>
      </form>

      {successMessage && <p className="error-message">{successMessage}</p>}

      {isAddModalOpen && (
        <div className="customer-modal-overlay">
          <div className="customer-modal-content">
            <div className="customer-modal-header">
              <h2>Success!</h2>
              <button className="customer-modal-close" onClick={closeAddModal}>
                &times;
              </button>
            </div>
            <p>Pet has been successfully added.</p>
            <button
              onClick={closeAddModal}
              className="button-base primary-button mt-4"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AddPetForm;
