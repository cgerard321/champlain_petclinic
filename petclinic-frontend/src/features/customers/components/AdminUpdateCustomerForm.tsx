import { useEffect, useState, FC } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getCustomer } from '../api/getCustomer.ts';
import { updateCustomer } from '../api/updateCustomer.ts';
import { getUserDetails } from '../api/getUserDetails';
import { updateUsername } from '../api/updateUsername';
import { CustomerRequestModel } from '../models/CustomerRequestModel.ts';
import { CustomerResponseModel } from '../models/CustomerResponseModel.ts';
import { UserDetailsModel } from '../models/UserDetailsModel';
import { useUsernameValidation } from '../hooks/useUsernameValidation';
import './UpdateCustomerForm.css';

const provincesOfCanada = [
  'Alberta',
  'British Columbia',
  'Manitoba',
  'New Brunswick',
  'Newfoundland and Labrador',
  'Nova Scotia',
  'Ontario',
  'Prince Edward Island',
  'Quebec',
  'Saskatchewan',
  'Northwest Territories',
  'Nunavut',
  'Yukon',
];

const AdminUpdateCustomerForm: FC = () => {
  const { t } = useTranslation('customers');
  const { customerId } = useParams<{ customerId: string }>();
  const navigate = useNavigate();
  const { validateUsernameField } = useUsernameValidation();
  const [formData, setFormData] = useState<CustomerRequestModel>({
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  });

  const [userDetails, setUserDetails] = useState<UserDetailsModel | null>(null);
  const [username, setUsername] = useState<string>('');
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    const fetchCustomerData = async (): Promise<void> => {
      if (!customerId) {
        console.error('Customer id is undefined');
        return;
      }

      try {
        const response = await getCustomer(customerId);
        const customerData: CustomerResponseModel = response.data;
        setFormData(customerData);
      } catch (error) {
        console.error('Error fetching customer data:', error);
      }
    };

    const fetchUserData = async (): Promise<void> => {
      if (!customerId) {
        console.error('Customer id is undefined');
        return;
      }

      try {
        const response = await getUserDetails(customerId);
        const userData: UserDetailsModel = response.data;
        setUserDetails(userData);
        setUsername(userData.username);
      } catch (error) {
        console.error('Error fetching user data:', error);
        setUserDetails({
          userId: customerId,
          username: 'Unknown',
          email: '',
          roles: [],
          verified: false,
          disabled: false,
        });
        setUsername('Unknown');
      }
    };

    fetchCustomerData().catch(error =>
      console.error('Error in fetchCustomerData:', error)
    );
    fetchUserData().catch(error =>
      console.error('Error in fetchUserData:', error)
    );
  }, [customerId]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleUsernameChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ): void => {
    setUsername(e.target.value);
    if (errors.username) {
      setErrors(prev => ({ ...prev, username: '' }));
    }
  };

  const validate = async (): Promise<boolean> => {
    const newErrors: { [key: string]: string } = {};

    if (!formData.firstName)
      newErrors.firstName = 'validation.firstNameRequired';
    if (!formData.lastName) newErrors.lastName = 'validation.lastNameRequired';
    if (!formData.address) newErrors.address = 'validation.addressRequired';
    if (!formData.city) newErrors.city = 'validation.cityRequired';
    if (!formData.province) newErrors.province = 'validation.provinceRequired';

    const telephoneRegex = /^[0-9]+$/;
    if (!formData.telephone) {
      newErrors.telephone = 'validation.telephoneRequired';
    } else if (!telephoneRegex.test(formData.telephone)) {
      newErrors.telephone = 'validation.telephoneDigitsOnly';
    }

    const usernameError = await validateUsernameField(
      username,
      userDetails?.username
    );
    if (usernameError) {
      newErrors.username = usernameError;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    e: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    e.preventDefault();
    if (!(await validate())) return;

    try {
      if (!customerId) {
        console.error('Customer id is undefined');
        return;
      }

      await updateCustomer(customerId, formData);

      if (userDetails && username !== userDetails.username) {
        await updateUsername(customerId, username);
      }

      setIsModalOpen(true);
    } catch (error) {
      console.error('Error updating customer:', error);
    }
  };

  const closeModal = (): void => {
    setIsModalOpen(false);
    navigate(`/customers/${customerId}`);
  };

  const handleBack = (): void => {
    navigate(`/customers/${customerId}`);
  };

  return (
    <div className="update-customer-form">
      <h1>{t('customerForm.title')}</h1>
      <form onSubmit={handleSubmit}>
        <label>{t('fields.username')} </label>
        <input
          type="text"
          name="username"
          value={username}
          onChange={handleUsernameChange}
        />
        {errors.username && <span className="error">{t(errors.username)}</span>}
        <br />
        <label>{t('fields.firstName')} </label>
        <input
          type="text"
          name="firstName"
          value={formData.firstName}
          onChange={handleChange}
        />
        {errors.firstName && (
          <span className="error">{t(errors.firstName)}</span>
        )}
        <br />
        <label>{t('fields.lastName')} </label>
        <input
          type="text"
          name="lastName"
          value={formData.lastName}
          onChange={handleChange}
        />
        {errors.lastName && <span className="error">{t(errors.lastName)}</span>}
        <br />
        <label>{t('fields.address')} </label>
        <input
          type="text"
          name="address"
          value={formData.address}
          onChange={handleChange}
        />
        {errors.address && <span className="error">{t(errors.address)}</span>}
        <br />
        <label>{t('fields.city')} </label>
        <input
          type="text"
          name="city"
          value={formData.city}
          onChange={handleChange}
        />
        {errors.city && <span className="error">{t(errors.city)}</span>}
        <br />
        <label>{t('fields.province')} </label>
        <select
          name="province"
          value={formData.province}
          onChange={handleChange}
        >
          <option value="">{t('customerForm.selectProvince')}</option>
          {provincesOfCanada.map(province => (
            <option key={province} value={province}>
              {province}
            </option>
          ))}
        </select>
        {errors.province && <span className="error">{t(errors.province)}</span>}
        <br />
        <label>{t('fields.telephone')} </label>
        <input
          type="text"
          name="telephone"
          value={formData.telephone}
          onChange={handleChange}
        />
        {errors.telephone && (
          <span className="error">{t(errors.telephone)}</span>
        )}
        <br />
        <button type="submit">{t('customerForm.submit')}</button>
      </form>

      <button id="back-button" onClick={handleBack}>
        {t('customerForm.back')}
      </button>

      {isModalOpen && (
        <div className="admin-update-customer-modal-overlay">
          <div className="admin-update-customer-modal">
            <h2>{t('customerForm.modal.title')}</h2>
            <p>{t('customerForm.modal.message')}</p>
            <button onClick={closeModal}>
              {t('customerForm.modal.close')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminUpdateCustomerForm;
