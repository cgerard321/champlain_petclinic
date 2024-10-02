import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { updatePet, getPet } from '../api/updatePet';
import { PetResponseModel } from '../models/PetResponseModel';
import { PetRequestModel } from '../models/PetRequestModel';
import { useNavigate, useParams } from 'react-router-dom';
import './UpdatePetForm.css';

const UpdatePetForm: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const { petId } = useParams<{ petId: string }>();
  const [pet, setPet] = useState<PetResponseModel | null>(null);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [notFound, setNotFound] = useState<boolean>(false);

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
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    if (pet) {
      const { name, type, value, checked } = e.target;
      setPet({
        ...pet,
        [name]: type === 'checkbox' ? (checked ? 'true' : 'false') : value, // Handle checkbox properly
      });
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
  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (!validate() || !pet) return;

    const petRequestData: PetRequestModel = {

      name: pet.name,
      birthDate: pet.birthDate, // Ensure date is in correct format
      petTypeId: pet.petTypeId,
      isActive: pet.isActive, // Already converted as string
      weight: pet.weight,
    };

    try {
      const response = await updatePet(petId!, petRequestData);
      if (response.status === 200) {
        setSuccessMessage('Pet updated successfully!');
        navigate(`/customers/${pet.ownerId}`);
      } else {
        console.error('Error updating pet');
      }
    } catch (error) {
      console.error('Error updating pet:', error);
    }
  };

  // Handle cancel button click
  const handleCancel = (): void => {
    if (pet) {
      navigate(`/customers/${pet.ownerId}`);
    }
  };

  if (notFound) {
    return <p>Pet not found. Please check the pet ID and try again.</p>;
  }

  if (!pet) {
    return <p>Loading...</p>;
  }

  return (
    <div className="update-pet-form">
      <h1>Edit Pet: {pet.petId}</h1>
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

        <label>Birth Date: </label>
        <input
          type="date"
          name="birthDate"
          value={pet.birthDate.toISOString().split('T')[0]}
          onChange={handleChange}
        />
        <br />

        <label>Pet Type: </label>
        <input
          type="text"
          name="petTypeId"
          value={pet.petTypeId}
          onChange={handleChange}
        />
        {errors.petTypeId && <span className="error">{errors.petTypeId}</span>}
        <br />

        <label>Is Active: </label>
        <input
          type="checkbox"
          name="isActive"
          checked={pet.isActive === 'true'} // Handle string conversion to checkbox
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
        <button type="button" onClick={handleCancel}>Cancel</button>
      </form>
      {successMessage && <p className="success">{successMessage}</p>}
    </div>
  );
};

export default UpdatePetForm;
