import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { getPet } from '../api/getPet';
import { updatePet } from '../api/updatePet';
import { deletePet } from '../api/deletePet';
import { getPetTypes } from '../api/getPetTypes';
import { deletePetPhoto } from '../api/deletePetPhoto';
import { PetResponseModel } from '../models/PetResponseModel';
import { PetRequestModel } from '../models/PetRequestModel';
import { PetTypeModel } from '../models/PetTypeModel';
import defaultProfile from '@/assets/Owners/defaultProfilePicture.png';
import { useConfirmModal } from '@/shared/hooks/useConfirmModal';
import axiosInstance from '@/shared/api/axiosInstance';
import './customers.css';

interface EditPetModalProps {
  isOpen: boolean;
  onClose: () => void;
  petId: string;
  ownerId: string;
  onPetUpdated?: (updatedPet?: PetResponseModel) => void;
  onPetDeleted?: () => void;
}

const EditPetModal: React.FC<EditPetModalProps> = ({
  isOpen,
  onClose,
  petId,
  ownerId,
  onPetUpdated,
  onPetDeleted,
}): JSX.Element => {
  const [pet, setPet] = useState<PetResponseModel | null>(null);
  const [dateInputValue, setDateInputValue] = useState<string>('');
  const [isDateInputFocused, setIsDateInputFocused] = useState<boolean>(false);
  const [petTypes, setPetTypes] = useState<PetTypeModel[]>([]);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [notFound, setNotFound] = useState<boolean>(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [petPhotoUrl, setPetPhotoUrl] = useState<string>('');
  const { confirm, ConfirmModal } = useConfirmModal();

  const fetchPetPhotoUrl = async (
    petId: string,
    petName: string
  ): Promise<string> => {
    try {
      const response = await axiosInstance.get(`/pets/${petId}`, {
        useV2: false,
        params: { includePhoto: true },
      });
      const petData = response.data;

      if (
        petData.photo &&
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (petData.photo.data || (petData.photo as any).fileData)
      ) {
        const base64Data =
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          petData.photo.data || (petData.photo as any).fileData;
        const contentType =
          petData.photo.contentType ||
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (petData.photo as any).fileType ||
          'image/png';
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: contentType });
        return URL.createObjectURL(blob);
      } else {
        return defaultProfile;
      }
    } catch (error) {
      console.error(`Error fetching photo for ${petName} (${petId}):`, error);
      return defaultProfile;
    }
  };

  const handleDeletePetPhoto = async (): Promise<void> => {
    if (!pet) return;

    const confirmed = await confirm({
      title: 'Delete Pet Photo',
      message:
        "Are you sure you want to delete this pet's photo? This action cannot be undone.",
      confirmText: 'Delete',
      cancelText: 'Cancel',
      variant: 'danger',
      destructive: true,
    });

    if (!confirmed) return;

    try {
      await deletePetPhoto(ownerId, pet.petId);
      setPetPhotoUrl(defaultProfile);

      // Update pet data to reflect photo deletion
      setPet(prev => (prev ? { ...prev, photo: undefined } : null));

      setSuccessMessage('Pet photo deleted successfully!');
    } catch (error) {
      console.error('Error deleting pet photo:', error);
      setErrors({ submit: 'Failed to delete pet photo. Please try again.' });
    }
  };

  useEffect(() => {
    const fetchPetData = async (): Promise<void> => {
      if (petId && isOpen) {
        setSuccessMessage('');
        setErrors({});
        try {
          const response = await getPet(petId, ownerId);
          const petData: PetResponseModel = response.data;
          setPet({
            ...petData,
            birthDate: new Date(petData.birthDate),
          });
          setDateInputValue(
            petData.birthDate
              ? new Date(petData.birthDate).toISOString().split('T')[0]
              : ''
          );

          const photoUrl = await fetchPetPhotoUrl(petId, petData.name);
          setPetPhotoUrl(photoUrl);
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

    const fetchPetTypes = async (): Promise<void> => {
      try {
        const petTypesData = await getPetTypes();
        setPetTypes(petTypesData);
      } catch (error) {
        console.error('Error fetching pet types:', error);
        setPetTypes([]);
      }
    };

    if (isOpen) {
      fetchPetData();
      fetchPetTypes();
    }
  }, [petId, ownerId, isOpen]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, type, value } = e.target;
    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setPet(prev => (prev ? { ...prev, [name]: checked } : null));
    } else if (type === 'date') {
      setDateInputValue(value);

      if (value && value.length === 10) {
        const dateValue = new Date(value);
        if (!isNaN(dateValue.getTime())) {
          setPet(prev => (prev ? { ...prev, [name]: dateValue } : null));
        }
      } else if (!value || value.length < 10) {
      }
    } else {
      setPet(prev => (prev ? { ...prev, [name]: value } : null));
    }
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!pet?.name?.trim()) newErrors.name = 'Pet name is required';
    if (!pet?.weight?.trim()) newErrors.weight = 'Weight is required';
    if (!pet?.petTypeId) newErrors.petTypeId = 'Pet type is required';
    if (pet?.weight && parseFloat(pet.weight) <= 0) {
      newErrors.weight = 'Weight must be greater than 0';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!pet || !validate()) return;

    setIsSubmitting(true);
    try {
      const petRequest: PetRequestModel = {
        ownerId: ownerId,
        name: pet.name,
        petTypeId: pet.petTypeId,
        isActive: pet.isActive ? 'true' : 'false',
        weight: pet.weight,
        birthDate: pet.birthDate,
      };

      const updateResponse = await updatePet(petId, petRequest);
      setSuccessMessage('Pet updated successfully!');

      if (updateResponse.data) {
        const updatedPetData = {
          ...updateResponse.data,
          birthDate: updateResponse.data.birthDate
            ? new Date(updateResponse.data.birthDate)
            : new Date(),
        };
        setPet(updatedPetData);
        setDateInputValue(
          updatedPetData.birthDate
            ? updatedPetData.birthDate.toISOString().split('T')[0]
            : ''
        );
      }

      if (onPetUpdated) {
        onPetUpdated(updateResponse.data);
      }

      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (error) {
      console.error('Error updating pet:', error);
      setErrors({ submit: 'Failed to update pet. Please try again.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (): Promise<void> => {
    try {
      await deletePet(petId);
      if (onPetDeleted) {
        onPetDeleted();
      }
      onClose();
    } catch (error) {
      console.error('Error deleting pet:', error);
      setErrors({ submit: 'Failed to delete pet. Please try again.' });
    }
  };

  const handleCancel = (): void => {
    onClose();
  };

  const closeDeleteModal = (): void => {
    setIsDeleteModalOpen(false);
  };

  const handleOverlayClick = (e: React.MouseEvent<HTMLDivElement>): void => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  if (!isOpen) return <></>;

  if (notFound) {
    return (
      <div className="customer-modal-overlay" onClick={handleOverlayClick}>
        <div
          className="customer-modal-content"
          onClick={e => e.stopPropagation()}
        >
          <div className="customer-modal-header">
            <h2>Pet Not Found</h2>
            <button className="customer-modal-close" onClick={onClose}>
              ×
            </button>
          </div>
          <p>Pet not found. Please check the pet ID and try again.</p>
          <div className="form-actions">
            <button onClick={onClose} className="secondary-button">
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!pet) {
    return (
      <div className="customer-modal-overlay" onClick={handleOverlayClick}>
        <div
          className="customer-modal-content"
          onClick={e => e.stopPropagation()}
        >
          <div className="customer-modal-header">
            <h2>Loading...</h2>
            <button className="customer-modal-close" onClick={onClose}>
              ×
            </button>
          </div>
          <p>Loading pet data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="customer-modal-overlay" onClick={handleOverlayClick}>
      <div
        className="customer-modal-content"
        onClick={e => e.stopPropagation()}
      >
        <div className="customer-modal-header">
          <h2>Edit Pet: {pet.name}</h2>
          <button className="customer-modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="pet-photo-section">
          <div className="pet-photo-container">
            <img
              src={petPhotoUrl || defaultProfile}
              alt={`${pet.name} profile`}
              className="pet-photo"
            />
            {petPhotoUrl && petPhotoUrl !== defaultProfile && pet.photo && (
              <button
                type="button"
                onClick={handleDeletePetPhoto}
                className="delete-photo-button"
                disabled={isSubmitting}
              >
                Delete Photo
              </button>
            )}
          </div>
        </div>

        <form onSubmit={handleSubmit} className="customer-add-pet-form">
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
              disabled={isSubmitting}
            >
              <option value="">Select a pet type</option>
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
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="isActive"
                checked={String(pet.isActive) === 'true'}
                onChange={handleChange}
                disabled={isSubmitting}
              />
              Is Active
            </label>
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

          <div className="form-group">
            <label>Birth Date</label>
            <input
              type="date"
              name="birthDate"
              value={
                isDateInputFocused
                  ? dateInputValue
                  : pet.birthDate && !isNaN(pet.birthDate.getTime())
                    ? pet.birthDate.toISOString().split('T')[0]
                    : ''
              }
              onChange={handleChange}
              onFocus={() => setIsDateInputFocused(true)}
              onBlur={() => setIsDateInputFocused(false)}
              disabled={isSubmitting}
            />
          </div>

          {errors.submit && (
            <div className="error-message">{errors.submit}</div>
          )}

          {successMessage && (
            <div className="success-message">{successMessage}</div>
          )}

          <div className="form-actions">
            <button
              type="button"
              onClick={handleCancel}
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
              {isSubmitting ? 'Updating...' : 'Update Pet'}
            </button>
            <button
              type="button"
              onClick={() => setIsDeleteModalOpen(true)}
              className="delete-button"
              disabled={isSubmitting}
            >
              Delete Pet
            </button>
          </div>
        </form>

        {isDeleteModalOpen && (
          <div className="customer-modal-overlay">
            <div className="customer-modal-content">
              <div className="customer-modal-header">
                <h2>Confirm Delete</h2>
                <button
                  className="customer-modal-close"
                  onClick={closeDeleteModal}
                >
                  ×
                </button>
              </div>
              <p>
                Are you sure you want to delete this pet? This action cannot be
                undone.
              </p>
              <div className="form-actions">
                <button onClick={closeDeleteModal} className="secondary-button">
                  Cancel
                </button>
                <button onClick={handleDelete} className="delete-button">
                  Delete
                </button>
              </div>
            </div>
          </div>
        )}
        <ConfirmModal />
      </div>
    </div>
  );
};

export default EditPetModal;
