import { FormEvent, useState } from 'react';
import * as PropTypes from 'prop-types';
import { addPetForOwner } from '../api/addPetForOwner';
import { PetRequestModel } from '../models/PetRequestModel';
import { PetResponseModel } from '../models/PetResponseModel';
import './AddPetModal.css';

interface AddPetModalProps {
  ownerId: string;
  isOpen: boolean;
  onClose: () => void;
  onPetAdded: (pet: PetResponseModel) => void;
}

const petTypeOptions: { [key: string]: string } = {
  '1': 'Cat',
  '2': 'Dog',
  '3': 'Lizard',
  '4': 'Snake',
  '5': 'Bird',
  '6': 'Hamster',
};

const AddPetModal: React.FC<AddPetModalProps> = ({
  ownerId,
  isOpen,
  onClose,
  onPetAdded,
}): JSX.Element | null => {
  const [pet, setPet] = useState<PetRequestModel>({
    ownerId,
    name: '',
    birthDate: new Date(),
    petTypeId: '',
    isActive: 'true',
    weight: '',
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  if (!isOpen) return null;

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, type, value } = e.target;
    if (type === 'date') {
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
    if (!pet.name.trim()) newErrors.name = 'Name is required';
    if (!pet.weight.trim()) newErrors.weight = 'Weight is required';
    if (!pet.petTypeId) newErrors.petTypeId = 'Pet Type is required';
    if (parseFloat(pet.weight) <= 0)
      newErrors.weight = 'Weight must be greater than 0';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      const response = await addPetForOwner(ownerId, pet);
      if (response.status === 201) {
        onPetAdded(response.data);
        handleClose();
      }
    } catch (error) {
      console.error('Error adding pet:', error);
      setErrors({ submit: 'Failed to add pet. Please try again.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = (): void => {
    setPet({
      ownerId,
      name: '',
      birthDate: new Date(),
      petTypeId: '',
      isActive: 'true',
      weight: '',
    });
    setErrors({});
    setIsSubmitting(false);
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Add New Pet</h2>
          <button className="modal-close" onClick={handleClose}>
            &times;
          </button>
        </div>

        <form onSubmit={handleSubmit} className="add-pet-form">
          <div className="form-group">
            <label htmlFor="name">Pet Name *</label>
            <input
              id="name"
              type="text"
              name="name"
              value={pet.name}
              onChange={handleChange}
              className={errors.name ? 'error-input' : ''}
              disabled={isSubmitting}
            />
            {errors.name && (
              <span className="error-message">{errors.name}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="petTypeId">Pet Type *</label>
            <select
              id="petTypeId"
              name="petTypeId"
              value={pet.petTypeId}
              onChange={handleChange}
              className={errors.petTypeId ? 'error-input' : ''}
              disabled={isSubmitting}
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
            <label htmlFor="birthDate">Birth Date</label>
            <input
              id="birthDate"
              type="date"
              name="birthDate"
              value={pet.birthDate.toISOString().split('T')[0]}
              onChange={handleChange}
              disabled={isSubmitting}
            />
          </div>

          <div className="form-group">
            <label htmlFor="weight">Weight (kg) *</label>
            <input
              id="weight"
              type="number"
              step="0.1"
              min="0.1"
              name="weight"
              value={pet.weight}
              onChange={handleChange}
              className={errors.weight ? 'error-input' : ''}
              disabled={isSubmitting}
            />
            {errors.weight && (
              <span className="error-message">{errors.weight}</span>
            )}
          </div>

          {errors.submit && (
            <div className="error-message">{errors.submit}</div>
          )}

          <div className="form-actions">
            <button
              type="button"
              onClick={handleClose}
              className="cancel-button"
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="submit-button"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Adding...' : 'Add Pet'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

AddPetModal.propTypes = {
  ownerId: PropTypes.string.isRequired,
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onPetAdded: PropTypes.func.isRequired,
};

export default AddPetModal;
