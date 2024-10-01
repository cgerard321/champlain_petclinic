import React, { useState, useEffect, FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getOwnerPets, updateOwnerPets } from '../api/updatePet';
import { PetRequestModel } from '../models/PetRequestModel';
import './UpdatePetForm.css';

const UpdateOwnerPetsForm: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const { ownerId } = useParams<{ ownerId: string }>();
  const [pets, setPets] = useState<PetRequestModel[]>([]);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  useEffect(() => {
    const fetchPetsData = async (): Promise<void> => {
      if (ownerId) {
        try {
          const response = await getOwnerPets(ownerId);
          setPets(response.data);
        } catch (error) {
          console.error('Error fetching pets data:', error);
        }
      }
    };
    fetchPetsData();
  }, [ownerId]);

  const handleChange = (index: number, e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>): void => {
    const { name, value } = e.target;
    const updatedPets = [...pets];
    updatedPets[index] = { ...updatedPets[index], [name]: value };
    setPets(updatedPets);
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    pets.forEach((pet, index) => {
      if (!pet.name) newErrors[`name${index}`] = 'Name is required';
      if (!pet.weight) newErrors[`weight${index}`] = 'Weight is required';
    });
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    try {
      await updateOwnerPets(ownerId!, pets);
      navigate(`/owners/${ownerId}`);
    } catch (error) {
      console.error('Error updating pets:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {pets.map((pet, index) => (
        <div key={pet.petId}>
          <h3>Edit Pet {index + 1}: {pet.name}</h3>

          <label>Name:</label>
          <input
            type="text"
            name="name"
            value={pet.name}
            onChange={e => handleChange(index, e)}
          />
          {errors[`name${index}`] && <span className="error">{errors[`name${index}`]}</span>}

          <label>Birth Date:</label>
          <input
            type="date"
            name="birthDate"
            value={new Date(pet.birthDate).toISOString().split('T')[0]} // Pre-fill birth date
            onChange={e => handleChange(index, e)}
          />

          <label>Pet Type:</label>
          <select
            name="petTypeId"
            value={pet.petTypeId}
            onChange={e => handleChange(index, e)}
          >
            <option value="1">Cat</option>
            <option value="2">Dog</option>
            <option value="3">Lizard</option>
            <option value="4">Snake</option>
            <option value="5">Bird</option>
            <option value="6">Hamster</option>
          </select>

          <label>Active Status:</label>
          <select
            name="isActive"
            value={pet.isActive}
            onChange={e => handleChange(index, e)}
          >
            <option value="true">Active</option>
            <option value="false">Inactive</option>
          </select>

          <label>Weight:</label>
          <input
            type="text"
            name="weight"
            value={pet.weight}
            onChange={e => handleChange(index, e)}
          />
          {errors[`weight${index}`] && <span className="error">{errors[`weight${index}`]}</span>}
        </div>
      ))}
      <button type="submit">Update Pets</button>
    </form>
  );
};

export default UpdateOwnerPetsForm;
