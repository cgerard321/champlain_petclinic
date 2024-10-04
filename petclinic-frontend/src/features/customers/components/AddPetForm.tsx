import * as React from 'react';
import { FormEvent, useState } from 'react';
import { addPet } from '../api/addPet';
import { PetRequestModel } from '../models/PetRequestModel';
import { useNavigate, useParams } from 'react-router-dom';
import './AddPetForm.css';

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
    if (!validate() || !ownerId) return;
    try {
      const response = await addPet(pet);
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

  // Modal handlers
  const closeAddModal = (): void => {
    setIsAddModalOpen(false);
    navigate(`/customers/${ownerId}`);
  };

  return (
   <div className="add-pet-form">
     <h1>Add New Pet</h1>
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

       <label>Birth Date: </label>
       <input
        type="date"
        name="birthDate"
        value={pet.birthDate.toISOString().split('T')[0]}
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

       <button type="submit">Add Pet</button>
       <button
        type="button"
        onClick={() => navigate(`/customers/${ownerId}`)}
        className={'cancel-form-button'}
       >
         Cancel
       </button>
     </form>
     {successMessage && <p className="success">{successMessage}</p>}

     {isAddModalOpen && (
      <div className="pet-add-modal-overlay">
        <div className="pet-add-modal">
          <h2>Success!</h2>
          <p>Pet has been successfully added.</p>
          <button onClick={closeAddModal}>Close</button>
        </div>
      </div>
     )}
   </div>
  );
};

export default AddPetForm;
