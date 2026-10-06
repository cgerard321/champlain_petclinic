import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { updatePet } from '../api/updatePet';
import { PetResponseModel } from '../models/PetResponseModel';
import { PetRequestModel } from '../models/PetRequestModel';

import './customers.css';
import { PetTypeModel } from '../models/PetTypeModel';
import { deletePet } from '../api/deletePet';
import {
  MAX_PET_AGE,
  MAX_PET_NAME_LENGTH,
  MAX_PET_WEIGHT,
} from '../utils/petValidationLimits';
import { useTranslation } from 'react-i18next';

interface UpdatePetFormProps {
  pet: PetResponseModel;
  customerId: string;
  petTypes: PetTypeModel[];
  onCancel: () => void;
  onPetUpdated?: (updatedPet?: PetResponseModel) => void;
  onPetDeleted?: () => void;
}

const UpdatePetForm: React.FC<UpdatePetFormProps> = ({
  pet: initialPet,
  customerId,
  petTypes,
  onCancel,
  onPetUpdated,
  onPetDeleted,
}): JSX.Element => {
  const { t } = useTranslation('customers');
  const [pet, setPet] = useState<PetResponseModel>({
    ...initialPet,
    birthDate: initialPet.birthDate
      ? new Date(initialPet.birthDate)
      : new Date(),
  });

  const [dateInputValue, setDateInputValue] = useState<string>(
    initialPet.birthDate
      ? new Date(initialPet.birthDate).toISOString().split('T')[0]
      : ''
  );

  const [isDateInputFocused, setIsDateInputFocused] = useState<boolean>(false);

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  useEffect(() => {
    const normalizedPet: PetResponseModel = {
      ...initialPet,
      birthDate: initialPet.birthDate
        ? new Date(initialPet.birthDate)
        : new Date(),
    };

    setPet(normalizedPet);

    setDateInputValue(
      normalizedPet.birthDate
        ? normalizedPet.birthDate.toISOString().split('T')[0]
        : ''
    );

    setErrors({});
    setSuccessMessage('');
  }, [initialPet]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, type, value } = e.target;

    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;

      setPet(prev => ({
        ...prev,
        [name]: checked ? 'true' : 'false',
      }));
    } else if (type === 'date') {
      setDateInputValue(value);

      if (value && value.length === 10) {
        const dateValue = new Date(value);

        if (!isNaN(dateValue.getTime())) {
          setPet(prev => ({
            ...prev,
            [name]: dateValue,
          }));
        }
      }
    } else {
      setPet(prev => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};

    if (!pet.name?.trim()) {
      newErrors.name = 'pets.errors.nameRequired';
    } else if (pet.name.trim().length > MAX_PET_NAME_LENGTH) {
      newErrors.name = 'pets.errors.nameTooLong';
    }

    if (!pet.weight?.trim()) {
      newErrors.weight = 'pets.errors.weightRequired';
    } else if (parseFloat(pet.weight) <= 0) {
      newErrors.weight = 'pets.errors.weightPositive';
    } else if (parseFloat(pet.weight) > MAX_PET_WEIGHT) {
      newErrors.weight = 'pets.errors.weightTooHigh';
    }

    if (!pet.petTypeId) {
      newErrors.petTypeId = 'pets.errors.typeRequired';
    }

    const oldestBirthDate = new Date();
    oldestBirthDate.setFullYear(oldestBirthDate.getFullYear() - MAX_PET_AGE);

    if (pet.birthDate < oldestBirthDate) {
      newErrors.birthDate = 'pets.errors.ageTooHigh';
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (!validate()) return;

    setIsSubmitting(true);
    setSuccessMessage('');

    try {
      const petRequestData: PetRequestModel = {
        customerId,
        name: pet.name,
        birthDate: pet.birthDate,
        petTypeId: pet.petTypeId,
        isActive: String(pet.isActive) === 'true' ? 'true' : 'false',
        weight: pet.weight,
      };

      const response = await updatePet(pet.petId, petRequestData);

      setSuccessMessage('pets.success.updated');
      onPetUpdated?.(response.data);
    } catch (error) {
      console.error('Error updating pet:', error);
      setErrors({ submit: 'pets.errors.updateFailed' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (): Promise<void> => {
    const confirmed = window.confirm(t('pets.deleteModal.message'));

    if (!confirmed) return;

    setIsSubmitting(true);

    try {
      await deletePet(pet.petId);
      onPetDeleted?.();
    } catch (error) {
      console.error('Error deleting pet:', error);
      setErrors({ submit: 'pets.errors.deleteFailed' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="form-container">
      <h1>{t('pets.editTitle', { name: pet.name })}</h1>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>{t('pets.fields.name')}</label>
          <input
            type="text"
            name="name"
            value={pet.name}
            onChange={handleChange}
            className={errors.name ? 'error-input' : ''}
          />
          {errors.name && (
            <span className="error-message">{t(errors.name)}</span>
          )}
        </div>

        <div className="form-group">
          <label>{t('pets.fields.petType')}</label>
          <select
            name="petTypeId"
            value={pet.petTypeId}
            onChange={handleChange}
            className={errors.petTypeId ? 'error-input' : ''}
          >
            <option value="">{t('pets.selectType')}</option>
            {petTypes.map(type => (
              <option key={type.petTypeId} value={type.petTypeId}>
                {t(`pets.types.${type.name.toLowerCase()}`, {
                  defaultValue: type.name,
                })}
              </option>
            ))}
          </select>
          {errors.petTypeId && (
            <span className="error-message">{t(errors.petTypeId)}</span>
          )}
        </div>

        <div className="form-group">
          <label>{t('pets.fields.isActive')}</label>
          <input
            type="checkbox"
            name="isActive"
            checked={pet.isActive === 'true'}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label>{t('pets.fields.weight')}</label>
          <input
            type="text"
            name="weight"
            value={pet.weight}
            onChange={handleChange}
            className={errors.weight ? 'error-input' : ''}
          />
          {errors.weight && (
            <span className="error-message">{t(errors.weight)}</span>
          )}
        </div>
        <div className="form-group">
          <label htmlFor={`pet-birth-date-${pet.petId}`}>
            {t('pets.fields.birthDate')}
          </label>

          <input
            id={`pet-birth-date-${pet.petId}`}
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
            className={errors.birthDate ? 'error-input' : ''}
            disabled={isSubmitting}
          />

          {errors.birthDate && (
            <span className="error-message">
              {t(errors.birthDate, {
                maxAge: MAX_PET_AGE,
              })}
            </span>
          )}
        </div>

        <div className="pet-inline-edit-actions">
          <button
            type="submit"
            className="button-base primary-button"
            disabled={isSubmitting}
          >
            {t('pets.buttons.update')}
          </button>
          <button
            type="button"
            onClick={handleDelete}
            className="button-base danger-button"
            disabled={isSubmitting}
          >
            {t('pets.buttons.delete')}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="button-base secondary-button"
            disabled={isSubmitting}
          >
            {t('pets.buttons.cancel')}
          </button>
        </div>
      </form>

      {successMessage && <p className="error-message">{t(successMessage)}</p>}
    </div>
  );
};

export default UpdatePetForm;
