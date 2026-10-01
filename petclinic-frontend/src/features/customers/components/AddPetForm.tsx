import * as React from 'react';
import { FormEvent, useState } from 'react';
import { addPetForCustomer } from '../api/addPetForCustomer.ts';
import { PetRequestModel } from '../models/PetRequestModel';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import './customers.css';

const petTypeOptions: { [key: string]: string } = {
  '1': 'pets.types.cat',
  '2': 'pets.types.dog',
  '3': 'pets.types.lizard',
  '4': 'pets.types.snake',
  '5': 'pets.types.bird',
  '6': 'pets.types.hamster',
};

const AddPetForm: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const { t } = useTranslation('customers');
  const { customerId } = useParams<{ customerId: string }>();
  const [pet, setPet] = useState<PetRequestModel>({
    customerId: customerId || '',
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
    if (!validate() || !customerId) return;
    try {
      const response = await addPetForCustomer(customerId, pet);
      if (response.status === 201) {
        setSuccessMessage('pets.success.added');
        setIsAddModalOpen(true);
      } else {
        console.error('Error adding pet');
      }
    } catch (error) {
      console.error('Error adding pet:', error);
    }
  };

  const closeAddModal = (): void => {
    setIsAddModalOpen(false);
    navigate(`/customers/${customerId}`);
  };

  return (
    <div className="form-container">
      <h1>{t('pets.addTitle')}</h1>
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
          <label>{t('pets.fields.birthDate')}</label>
          <input
            type="date"
            name="birthDate"
            value={pet.birthDate.toISOString().split('T')[0]}
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
            {t('pets.buttons.add')}
          </button>
          <button
            type="button"
            onClick={() => navigate(`/customers/${customerId}`)}
            className="button-base secondary-button mt-2"
          >
            {t('pets.buttons.cancel')}
          </button>
        </div>
      </form>

      {successMessage && <p className="error-message">{t(successMessage)}</p>}

      {isAddModalOpen && (
        <div className="customer-modal-overlay">
          <div className="customer-modal-content">
            <div className="customer-modal-header">
              <h2>{t('pets.success.title')}</h2>
              <button className="customer-modal-close" onClick={closeAddModal}>
                &times;
              </button>
            </div>
            <p>{t('pets.success.addedMessage')}</p>
            <button
              onClick={closeAddModal}
              className="button-base primary-button mt-4"
            >
              {t('pets.buttons.close')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AddPetForm;
