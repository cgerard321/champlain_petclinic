import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { updatePet, getPet } from '../api/updatePet';
import { PetResponseModel } from '../models/PetResponseModel';
import { PetRequestModel } from '../models/PetRequestModel';
import { useNavigate, useParams } from 'react-router-dom';
import './UpdatePetForm.css';
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
  const { petId } = useParams<{ petId: string }>();
  const [pet, setPet] = useState<PetResponseModel | null>(null);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [notFound, setNotFound] = useState<boolean>(false);
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  // Fetch pet data on component mount
  useEffect(() => {
    const fetchPetData = async (): Promise<void> => {
      if (petId) {
        try {
          const response = await getPet(petId);
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
  }, [petId]);

  // Handle form field changes
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

  // Form validation
  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!pet?.name) newErrors.name = 'Name is required';
    if (!pet?.weight) newErrors.weight = 'Weight is required';
    if (!pet?.petTypeId) newErrors.petTypeId = 'Pet Type is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate() || !pet) return;
    const petRequestData: PetRequestModel = {
      ownerId: pet.ownerId,
      name: pet.name,
      birthDate: pet.birthDate,
      petTypeId: pet.petTypeId,
      isActive: pet.isActive,
      weight: pet.weight,
    };

    try {
      const response = await updatePet(petId!, petRequestData);
      if (response.status === 200) {
        setSuccessMessage('Pet updated successfully!');
        setIsUpdateModalOpen(true);
      } else {
        console.error('Error updating pet');
      }
    } catch (error) {
      console.error('Error updating pet:', error);
    }
  };

  // Handle delete pet
  const handleDelete = async (): Promise<void> => {
    if (petId) {
      try {
        const response = await deletePet(petId);
        if (response.status === 200) {
          navigate(`/customers/${response.data.ownerId}`); // Redirect after deletion
        } else {
          console.error('Error deleting pet');
        }
      } catch (error) {
        console.error('Error deleting pet:', error);
      }
    }
  };

  // Handle cancel button click
  const handleCancel = (): void => {
    if (pet) {
      navigate(`/customers/${pet.ownerId}`);
    }
  };

  // Modal handlers
  const closeUpdateModal = (): void => {
    setIsUpdateModalOpen(false);
    navigate(`/customers/${pet?.ownerId}`); // Redirect after closing the update modal
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
    <div className="update-pet-form">
      <h1>Edit Pet: {pet.name}</h1>
      <form onSubmit={handleSubmit}>
        <label>Name: </label>
        <input
          type="text"
          name="name"
          value={pet.name}
          onChange={handleChange}
        />
        {errors.name && <span className="error">{errors.name}</span>}
        <br />

        <label>Pet Type: </label>
        <select name="petTypeId" value={pet.petTypeId} onChange={handleChange}>
          <option value="">Select a pet type</option>
          {Object.entries(petTypeOptions).map(([id, name]) => (
            <option key={id} value={id}>
              {name}
            </option>
          ))}
        </select>
        {errors.petTypeId && <span className="error">{errors.petTypeId}</span>}
        <br />

        <label>Is Active: </label>
        <input
          type="checkbox"
          name="isActive"
          checked={pet.isActive === 'true'}
          onChange={handleChange}
        />
        <br />

        <label>Weight (kg): </label>
        <input
          type="text"
          name="weight"
          value={pet.weight}
          onChange={handleChange}
        />
        {errors.weight && <span className="error">{errors.weight}</span>}
        <br />

        <button type="submit">Update Pet</button>
        <button
          type="button"
          onClick={() => setIsDeleteModalOpen(true)} // Open delete modal
          className={'delete-pet-button'}
        >
          Delete Pet
        </button>
        <button
          type="button"
          onClick={handleCancel}
          className={'cancel-form-button'}
        >
          Cancel
        </button>
      </form>
      {successMessage && <p className="success">{successMessage}</p>}

      {isUpdateModalOpen && (
        <div className="pet-update-modal-overlay">
          <div className="pet-update-modal">
            <h2>Success!</h2>
            <p>Pet has been successfully updated.</p>
            <button onClick={closeUpdateModal}>Close</button>
          </div>
        </div>
      )}

      {isDeleteModalOpen && (
        <div className="pet-delete-modal-overlay">
          <div className="pet-delete-modal">
            <h2>Confirm Deletion</h2>
            <p>Are you sure you want to delete this pet?</p>
            <button onClick={handleDelete}>Yes, Delete</button>
            <button onClick={closeDeleteModal} className={'cancel-form-button'}>
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UpdatePetForm;
