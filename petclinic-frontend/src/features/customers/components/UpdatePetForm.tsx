import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { getPet } from '../api/getPet';
import { updatePet } from '../api/updatePet';
import { PetResponseModel } from '../models/PetResponseModel';
import { PetRequestModel } from '../models/PetRequestModel';
import { useNavigate, useParams } from 'react-router-dom';
import './customers.css';
import { deletePet } from '@/features/customers/api/deletePet.ts';

const petTypeOptions: { [key: string]: string } = {
  '1': 'Cat',
  '2': 'Dog',
  '3': 'Lizard',
  '4': 'Snake',
  '5': 'Bird',
  '6': 'Hamster',
};

const UpdatePetForm: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const { ownerId, petId } = useParams<{ ownerId: string; petId: string }>();
  const [pet, setPet] = useState<PetResponseModel | null>(null);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [notFound, setNotFound] = useState<boolean>(false);
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  useEffect(() => {
    const fetchPetData = async (): Promise<void> => {
      if (petId) {
        try {
          const response = await getPet(petId, ownerId);
          const petData: PetResponseModel = response.data;
          setPet({
            ...petData,
            birthDate: new Date(petData.birthDate),
          });
        } catch (err) {
          const error = err as { response?: { status: number } };
          if (error.response && error.response.status === 404) {
            setNotFound(true);
          } else {
            console.error('Error fetching pet data:', error);
          }
        }
      }
    };
    fetchPetData().catch(error =>
      console.error('Error in fetchPetData:', error)
    );
  }, [petId, ownerId]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    if (pet) {
      const { name, type, value } = e.target;
      if (type === 'checkbox') {
        const checked = (e.target as HTMLInputElement).checked;
        setPet({
          ...pet,
          [name]: checked ? 'true' : 'false',
        });
      } else {
        setPet({
          ...pet,
          [name]: value,
        });
      }
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
    if (!validate() || !pet || !ownerId || !petId) return;
    const petRequestData: PetRequestModel = {
      ownerId: pet.ownerId,
      name: pet.name,
      birthDate: pet.birthDate,
      petTypeId: pet.petTypeId,
      isActive: pet.isActive,
      weight: pet.weight,
    };

    try {
      const response = await updatePet(petId, petRequestData);
      if (response.status === 200) {
        setSuccessMessage('Pet updated successfully!');
        setIsUpdateModalOpen(true);
      }
    } catch (error) {
      console.error('Error updating pet:', error);
    }
  };

  const handleDelete = async (): Promise<void> => {
    if (petId && ownerId) {
      try {
        const response = await deletePet(petId);
        if (response.status === 200) {
          navigate(`/customers/${response.data.ownerId}`);
        }
      } catch (error) {
        console.error('Error deleting pet:', error);
      }
    }
  };

  const handleCancel = (): void => {
    if (pet) {
      navigate(`/customers/${pet.ownerId}`);
    }
  };

  const closeUpdateModal = (): void => {
    setIsUpdateModalOpen(false);
    navigate(`/customers/${pet?.ownerId}`);
  };

  const closeDeleteModal = (): void => {
    setIsDeleteModalOpen(false);
  };

  if (notFound) {
    return <p>Pet not found. Please check the pet ID and try again.</p>;
  }

  if (!pet) {
    return <p>Loading...</p>;
  }

  return (
    <div className="form-container">
      <h1>Edit Pet: {pet.name}</h1>
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
          <label>Is Active</label>
          <input
            type="checkbox"
            name="isActive"
            checked={pet.isActive === 'true'}
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
            Update Pet
          </button>
          <button
            type="button"
            onClick={() => setIsDeleteModalOpen(true)}
            className="button-base danger-button mt-2"
          >
            Delete Pet
          </button>
          <button
            type="button"
            onClick={handleCancel}
            className="button-base secondary-button mt-2"
          >
            Cancel
          </button>
        </div>
      </form>

      {successMessage && <p className="error-message">{successMessage}</p>}

      {isUpdateModalOpen && (
        <div className="customer-modal-overlay">
          <div className="customer-modal-content">
            <div className="customer-modal-header">
              <h2>Success!</h2>
              <button
                className="customer-modal-close"
                onClick={closeUpdateModal}
              >
                &times;
              </button>
            </div>
            <p>Pet has been successfully updated.</p>
            <button
              onClick={closeUpdateModal}
              className="button-base primary-button mt-4"
            >
              Close
            </button>
          </div>
        </div>
      )}

      {isDeleteModalOpen && (
        <div className="customer-modal-overlay">
          <div className="customer-modal-content">
            <div className="customer-modal-header">
              <h2>Confirm Deletion</h2>
              <button
                className="customer-modal-close"
                onClick={closeDeleteModal}
              >
                &times;
              </button>
            </div>
            <p>Are you sure you want to delete this pet?</p>
            <button
              onClick={handleDelete}
              className="button-base danger-button mt-4"
            >
              Yes, Delete
            </button>
            <button
              onClick={closeDeleteModal}
              className="button-base secondary-button mt-4"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UpdatePetForm;
