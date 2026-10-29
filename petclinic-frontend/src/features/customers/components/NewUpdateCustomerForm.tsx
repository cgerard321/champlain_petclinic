import { FormEvent, useState } from 'react';
import { CustomerRequestModel } from '@/features/customers/models/CustomerRequestModel.ts';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel.ts';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { useUser } from '@/context/UserContext';
import { useToast } from '@/shared/components/toast/ToastProvider';
import { useUsernameValidation } from '../hooks/useUsernameValidation';

import { validateTelephone } from '../utils/validation';
import { provincesOfCanada } from '../utils/provinces';

import { updateCustomer } from '../api/updateCustomer.ts';
import { updateUsername } from '../api/updateUsername.ts';

import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';

import './UpdateCustomerForm.css';
import { useTranslation } from 'react-i18next';
import AddressAutocomplete from './AddressAutocomplete';
import { AddressSuggestionModel } from '../models/AddressSuggestionModel';

const NewUpdateCustomerForm = ({
  customer,
  userDetails,
  profilePicUrl,
  setCustomer,
  onBackButtonClick,
}: {
  customer: CustomerResponseModel;
  userDetails: UserDetailsModel;
  profilePicUrl: string;

  setCustomer: React.Dispatch<
    React.SetStateAction<CustomerResponseModel | null>
  >;

  onBackButtonClick: () => void;
}): JSX.Element => {
  const { t } = useTranslation('customers');
  const { user, checkSession } = useUser();
  const { showToast } = useToast();
  const { validateUsernameField } = useUsernameValidation();

  const [formCustomer, setFormCustomer] = useState<CustomerRequestModel>({
    firstName: customer.firstName,
    lastName: customer.lastName,
    address: customer.address,
    city: customer.city,
    province: customer.province,
    telephone: customer.telephone,
  });

  const [username, setUsername] = useState<string>(userDetails.username);

  const [errors, setErrors] = useState<{
    [key: string]: string;
  }>({});

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;

    setFormCustomer(prev => ({
      ...prev,
      [name]: value,
    }));

    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const handleAddressSelect = (suggestion: AddressSuggestionModel): void => {
    setFormCustomer(prev => ({
      ...prev,
      address: suggestion.address,
      city: suggestion.city,
      province: suggestion.province,
    }));
    setErrors(prev => ({ ...prev, address: '', city: '', province: '' }));
  };

  const handleUsernameChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ): void => {
    setUsername(e.target.value);

    if (errors.username) {
      setErrors(prev => ({
        ...prev,
        username: '',
      }));
    }
  };

  const validate = async (): Promise<boolean> => {
    const newErrors: { [key: string]: string } = {};

    if (!formCustomer.firstName) {
      newErrors.firstName = 'validation.firstNameRequired';
    }

    if (!formCustomer.lastName) {
      newErrors.lastName = 'validation.lastNameRequired';
    }

    if (!formCustomer.address) {
      newErrors.address = 'validation.addressRequired';
    }

    if (!formCustomer.city) {
      newErrors.city = 'validation.cityRequired';
    }

    if (!formCustomer.province) {
      newErrors.province = 'validation.provinceRequired';
    }

    const telephoneError = validateTelephone(formCustomer.telephone);

    if (telephoneError) {
      newErrors.telephone = telephoneError;
    }

    const usernameError = await validateUsernameField(
      username,
      userDetails.username
    );

    if (usernameError) {
      newErrors.username = usernameError;
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (!(await validate())) return;

    try {
      await updateCustomer(user.userId, formCustomer);

      setCustomer(prevCustomer => {
        if (!prevCustomer) return prevCustomer;

        return {
          ...prevCustomer,
          ...formCustomer,
        };
      });

      if (username !== userDetails.username) {
        await updateUsername(user.userId, username);
        await checkSession();
      }

      showToast(t('customerForm.updateSuccess'), 'success');
      window.location.reload();
      onBackButtonClick();
    } catch (error) {
      console.error('Error updating customer:', error);

      showToast(t('customerForm.updateError'), 'error');
    }
  };

  return (
    <div>
      <div className="customers-page customers-container-profile">
        <div className="customers-profile-card shadow-lg p-5 mb-5 bg-white rounded">
          <div
            className="customers-profile-header"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '1rem',
            }}
          >
            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <img
                src={profilePicUrl || defaultProfile}
                alt="Profile Picture"
                className="profile-picture"
              />
            </div>

            <h1>
              {customer.firstName} {customer.lastName}&apos;s Profile
            </h1>
          </div>

          <form onSubmit={handleSubmit} className="customers-profile-info">
            <p>
              <strong>{t('fields.username')}</strong>{' '}
              <input
                type="text"
                name="username"
                value={username}
                onChange={handleUsernameChange}
              />
              {errors.username && (
                <span className="error">{t(errors.username)}</span>
              )}
            </p>

            <p>
              <strong>{t('fields.firstName')} </strong>{' '}
              <input
                type="text"
                name="firstName"
                value={formCustomer.firstName}
                onChange={handleChange}
              />
              {errors.firstName && (
                <span className="error">{t(errors.firstName)}</span>
              )}
            </p>

            <p>
              <strong>{t('fields.lastName')}</strong>{' '}
              <input
                type="text"
                name="lastName"
                value={formCustomer.lastName}
                onChange={handleChange}
              />
              {errors.lastName && (
                <span className="error">{t(errors.lastName)}</span>
              )}
            </p>

            <p>
              <strong>{t('fields.address')}</strong>{' '}
              <AddressAutocomplete
                value={formCustomer.address}
                onChange={handleChange}
                onSelect={handleAddressSelect}
              />
              {errors.address && (
                <span className="error">{t(errors.address)}</span>
              )}
            </p>

            <p>
              <strong>{t('fields.city')}</strong>{' '}
              <input
                type="text"
                name="city"
                value={formCustomer.city}
                onChange={handleChange}
              />
              {errors.city && <span className="error">{t(errors.city)}</span>}
            </p>

            <p>
              <strong>{t('customerForm.selectProvince')}</strong>{' '}
              <select
                name="province"
                value={formCustomer.province}
                onChange={handleChange}
              >
                <option value="">{t('customerForm.selectProvince')}</option>

                {provincesOfCanada.map(province => (
                  <option key={province} value={province}>
                    {province}
                  </option>
                ))}
              </select>
              {errors.province && (
                <span className="error">{t(errors.province)}</span>
              )}
            </p>

            <p>
              <strong>{t('fields.telephone')}</strong>{' '}
              <input
                type="text"
                name="telephone"
                value={formCustomer.telephone}
                onChange={handleChange}
              />
              {errors.telephone && (
                <span className="error">{t(errors.telephone)}</span>
              )}
            </p>

            <div className="button-container">
              <button
                type="button"
                className="customers-backButton"
                onClick={onBackButtonClick}
              >
                {t('customerForm.back')}
              </button>

              <button type="submit" className="customers-updateButton">
                {t('customerForm.submit')}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default NewUpdateCustomerForm;
