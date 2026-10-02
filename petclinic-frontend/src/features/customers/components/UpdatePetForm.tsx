import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { getPet } from '../api/getPet';
import { updatePet } from '../api/updatePet';
import { PetResponseModel } from '../models/PetResponseModel';
import { PetRequestModel } from '../models/PetRequestModel';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import './customers.css';
import { deletePet } from '@/features/customers/api/deletePet.ts';

const petTypeOptions: { [key: string]: string } = {
  '1': 'pets.types.cat',
  '2': 'pets.types.dog',
  '3': 'pets.types.lizard',
  '4': 'pets.types.snake',
  '5': 'pets.types.bird',
  '6': 'pets.types.hamster',
};

const UpdatePetForm: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const { t } = useTranslation('customers');
  const { customerId, petId } = useParams<{
    customerId: string;
    petId: string;
  }>();
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
          const response = await getPet(petId, customerId);
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
  }, [petId, customerId]);

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
    if (!pet?.name) newErrors.name = 'pets.errors.nameRequired';
    if (!pet?.weight) newErrors.weight = 'pets.errors.weightRequired';
    if (!pet?.petTypeId) newErrors.petTypeId = 'pets.errors.typeRequired';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate() || !pet || !customerId || !petId) return;
    const petRequestData: PetRequestModel = {
      customerId: pet.customerId,
      name: pet.name,
      birthDate: pet.birthDate,
      petTypeId: pet.petTypeId,
      isActive: pet.isActive,
      weight: pet.weight,
    };

    try {
      const response = await updatePet(petId, petRequestData);
      if (response.status === 200) {
        setSuccessMessage('pets.success.updated');
        setIsUpdateModalOpen(true);
      }
    } catch (error) {
      console.error('Error updating pet:', error);
    }
  };

  const handleDelete = async (): Promise<void> => {
    if (petId && customerId) {
      try {
        const response = await deletePet(petId);
        if (response.status === 200) {
          navigate(`/customers/${response.data.customerId}`);
        }
      } catch (error) {
        console.error('Error deleting pet:', error);
      }
    }
  };

  const handleCancel = (): void => {
    if (pet) {
      navigate(`/customers/${pet.customerId}`);
    }
  };

  const closeUpdateModal = (): void => {
    setIsUpdateModalOpen(false);
    navigate(`/customers/${pet?.customerId}`);
  };

  const closeDeleteModal = (): void => {
    setIsDeleteModalOpen(false);
  };

  if (notFound) {
    return <p>{t('pets.notFound.message')}</p>;
  }

  if (!pet) {
    return <p>{t('pets.loading.title')}</p>;
  }

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
            {Object.entries(petTypeOptions).map(([id, name]) => (
              <option key={id} value={id}>
                {t(name)}
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

        <div className="form-group" style={{ textAlign: 'center' }}>
          <button type="submit" className="button-base primary-button">
            {t('pets.buttons.update')}
          </button>
          <button
            type="button"
            onClick={() => setIsDeleteModalOpen(true)}
            className="button-base danger-button mt-2"
          >
            {t('pets.buttons.delete')}
          </button>
          <button
            type="button"
            onClick={handleCancel}
            className="button-base secondary-button mt-2"
          >
            {t('pets.buttons.cancel')}
          </button>
        </div>
      </form>

      {successMessage && <p className="error-message">{t(successMessage)}</p>}

      {isUpdateModalOpen && (
        <div className="customer-modal-overlay">
          <div className="customer-modal-content">
            <div className="customer-modal-header">
              <h2>{t('pets.success.title')}</h2>
              <button
                className="customer-modal-close"
                onClick={closeUpdateModal}
              >
                &times;
              </button>
            </div>
            <p>{t('pets.success.updatedMessage')}</p>
            <button
              onClick={closeUpdateModal}
              className="button-base primary-button mt-4"
            >
              {t('pets.buttons.close')}
            </button>
          </div>
        </div>
      )}

      {isDeleteModalOpen && (
        <div className="customer-modal-overlay">
          <div className="customer-modal-content">
            <div className="customer-modal-header">
              <h2>{t('pets.deleteModal.title')}</h2>
              <button
                className="customer-modal-close"
                onClick={closeDeleteModal}
              >
                &times;
              </button>
            </div>
            <p>{t('pets.deleteModal.message')}</p>
            <button
              onClick={handleDelete}
              className="button-base danger-button mt-4"
            >
              {t('pets.buttons.yesDelete')}
            </button>
            <button
              onClick={closeDeleteModal}
              className="button-base secondary-button mt-4"
            >
              {t('pets.buttons.cancel')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UpdatePetForm;
