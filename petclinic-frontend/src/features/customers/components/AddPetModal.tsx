import { FormEvent, useState, useEffect, ChangeEvent } from 'react';
import * as PropTypes from 'prop-types';
import { addPetForOwner } from '../api/addPetForOwner';
import { addPetPhoto } from '../api/addPetPhoto';
import { getPetTypes } from '../api/getPetTypes';
import { PetRequestModel } from '../models/PetRequestModel';
import { PetResponseModel } from '../models/PetResponseModel';
import { PetTypeModel } from '../models/PetTypeModel';
import defaultProfile from '@/assets/Owners/defaultProfilePicture.png';
import './customers.css';

interface AddPetModalProps {
  ownerId: string;
  isOpen: boolean;
  onClose: () => void;
  onPetAdded: (pet: PetResponseModel) => void;
}

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
  const [dateInputValue, setDateInputValue] = useState<string>('');
  const [isDateInputFocused, setIsDateInputFocused] = useState<boolean>(false);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [petTypes, setPetTypes] = useState<PetTypeModel[]>([]);
  const [isLoadingPetTypes, setIsLoadingPetTypes] = useState(true);
  const [petPhotoUrl, setPetPhotoUrl] = useState<string>(defaultProfile);
  const [selectedPhoto, setSelectedPhoto] = useState<File | null>(null);

  useEffect(() => {
    const fetchPetTypes = async (): Promise<void> => {
      try {
        setIsLoadingPetTypes(true);
        const petTypesData = await getPetTypes();
        setPetTypes(petTypesData);
      } catch (error) {
        console.error('Error fetching pet types:', error);
        setPetTypes([]);
      } finally {
        setIsLoadingPetTypes(false);
      }
    };
    if (isOpen) {
      fetchPetTypes();
    }
  }, [isOpen]);

  useEffect(() => {
    if (isOpen) {
      setDateInputValue(pet.birthDate.toISOString().split('T')[0]);
    }
  }, [isOpen, pet.birthDate]);

  if (!isOpen) return null;

  const handleChange = (
      e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, type, value } = e.target;
    if (type === 'date') {
      setDateInputValue(value);
      if (value && value.length === 10) {
        const dateValue = new Date(value);
        if (!isNaN(dateValue.getTime())) {
          setPet({ ...pet, [name]: dateValue });
        }
      } else if (value === '') {
        setPet({ ...pet, [name]: new Date() });
      }
    } else {
      setPet({ ...pet, [name]: value });
    }
  };

  const handlePhotoChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedPhoto(file);
      setPetPhotoUrl(URL.createObjectURL(file));
    }
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!pet.name.trim()) newErrors.name = 'Pet name is required';
    if (!pet.weight.trim()) newErrors.weight = 'Weight is required';
    if (!pet.petTypeId) newErrors.petTypeId = 'Pet type is required';
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
        const newPet = response.data;

        // If a photo was selected, upload it with PATCH /pets/{petId}/photos
        if (selectedPhoto) {
          try {
            await addPetPhoto(newPet.petId, selectedPhoto);
          } catch (photoError) {
            console.error('Error uploading pet photo:', photoError);
          }
        }

        onPetAdded(newPet);
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
    setDateInputValue('');
    setIsDateInputFocused(false);
    setErrors({});
    setIsSubmitting(false);
    setPetPhotoUrl(defaultProfile);
    setSelectedPhoto(null);
    onClose();
  };

  return (
      <div className="customer-modal-overlay" onClick={handleClose}>
        <div
            className="customer-modal-content"
            onClick={e => e.stopPropagation()}
        >
          <div className="customer-modal-header">
            <h2>Add New Pet</h2>
            <button className="customer-modal-close" onClick={handleClose}>
              &times;
            </button>
          </div>

          <div className="pet-photo-section">
            <div className="pet-photo-container">
              <img
                  src={petPhotoUrl}
                  alt="New pet profile"
                  className="pet-photo"
              />
              <div className="file-input-row">
                <label htmlFor="pet-photo">Upload Photo:</label>
                <input
                    type="file"
                    id="pet-photo"
                    accept="image/*"
                    onChange={handlePhotoChange}
                    disabled={isSubmitting}
                />
              </div>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="add-pet-form">
            <div className="form-group">
              <label>Pet Name *</label>
              <input
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
              <label>Pet Type *</label>
              <select
                  name="petTypeId"
                  value={pet.petTypeId}
                  onChange={handleChange}
                  className={errors.petTypeId ? 'error-input' : ''}
                  disabled={isSubmitting || isLoadingPetTypes}
              >
                <option value="">
                  {isLoadingPetTypes
                      ? 'Loading pet types...'
                      : 'Select a pet type'}
                </option>
                {petTypes.map(type => (
                    <option key={type.petTypeId} value={type.petTypeId}>
                      {type.name}
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
                  value={
                    isDateInputFocused
                        ? dateInputValue
                        : dateInputValue ||
                        (pet.birthDate && !isNaN(pet.birthDate.getTime())
                            ? pet.birthDate.toISOString().split('T')[0]
                            : new Date().toISOString().split('T')[0])
                  }
                  onChange={handleChange}
                  onFocus={() => setIsDateInputFocused(true)}
                  onBlur={() => setIsDateInputFocused(false)}
                  disabled={isSubmitting}
              />
            </div>

            <div className="form-group">
              <label>Weight (kg) *</label>
              <input
                  type="number"
                  name="weight"
                  step="0.1"
                  min="0.1"
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
                  className="secondary-button"
                  disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                  type="submit"
                  className="primary-button"
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
