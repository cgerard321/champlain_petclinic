import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getCustomer } from '../api/getCustomer.ts';
import { updateCustomer } from '../api/updateCustomer.ts';
import { getUserDetails } from '../api/getUserDetails';
import { updateUsername } from '../api/updateUsername';
import { CustomerRequestModel } from '@/features/customers/models/CustomerRequestModel.ts';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel.ts';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useUser } from '@/context/UserContext';
import { useUsernameValidation } from '../hooks/useUsernameValidation';
import './UpdateCustomerForm.css';
import { validateTelephone } from '../utils/validation';
import { useToast } from '@/shared/components/toast/ToastProvider';
import { provincesOfCanada } from '../utils/provinces';

const UpdateCustomerForm: React.FC = (): JSX.Element => {
  const { t } = useTranslation('customers');
  const { showToast } = useToast();
  const navigate = useNavigate();
  const { user, checkSession } = useUser();
  const { validateUsernameField } = useUsernameValidation();
  const [customer, setCustomer] = useState<CustomerRequestModel>({
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

  useEffect(() => {
    const fetchData = async (): Promise<void> => {
      try {
        const [customerResponse, userResponse] = await Promise.all([
          getCustomer(user.userId),
          getUserDetails(user.userId),
        ]);

        const customerData: CustomerResponseModel = customerResponse.data;
        setCustomer({
          firstName: customerData.firstName,
          lastName: customerData.lastName,
          address: customerData.address,
          city: customerData.city,
          province: customerData.province,
          telephone: customerData.telephone,
        });

        const userData: UserDetailsModel = userResponse.data;
        setUserDetails(userData);
        setUsername(userData.username);
      } catch (error) {
        console.error('Error fetching data:', error);
        setUserDetails({
          userId: user.userId,
          username: 'Unknown',
          email: '',
          roles: [],
          verified: false,
          disabled: false,
        });
        setUsername('Unknown');
      }
    };

    fetchData();
  }, [user.userId]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;
    setCustomer({ ...customer, [name]: value });
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
    if (!customer.firstName)
      newErrors.firstName = 'validation.firstNameRequired';
    if (!customer.lastName) newErrors.lastName = 'validation.lastNameRequired';
    if (!customer.address) newErrors.address = 'validation.addressRequired';
    if (!customer.city) newErrors.city = 'validation.cityRequired';
    if (!customer.province) newErrors.province = 'validation.provinceRequired';

    const telephoneError = validateTelephone(customer.telephone);
    if (telephoneError) newErrors.telephone = telephoneError;

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
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!(await validate())) return;

    try {
      await updateCustomer(user.userId, customer);

      if (userDetails && username !== userDetails.username) {
        await updateUsername(user.userId, username);

        // Refresh the session so context (and navbar, etc.) picks up the new username
        await checkSession();
      }

      showToast(t('customerForm.updateSuccess'), 'success');
      navigate(AppRoutePaths.Home);
    } catch (error) {
      console.error('Error:', error);
      showToast(t('customerForm.updateError'), 'error');
    }
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
          value={customer.firstName}
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
          value={customer.lastName}
          onChange={handleChange}
        />
        {errors.lastName && <span className="error">{t(errors.lastName)}</span>}
        <br />
        <label>{t('fields.address')} </label>
        <input
          type="text"
          name="address"
          value={customer.address}
          onChange={handleChange}
        />
        {errors.address && <span className="error">{t(errors.address)}</span>}
        <br />
        <label>{t('fields.city')} </label>
        <input
          type="text"
          name="city"
          value={customer.city}
          onChange={handleChange}
        />
        {errors.city && <span className="error">{t(errors.city)}</span>}
        <br />
        <label>{t('fields.province')} </label>
        <select
          name="province"
          value={customer.province}
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
          value={customer.telephone}
          onChange={handleChange}
        />
        {errors.telephone && (
          <span className="error">{t(errors.telephone)}</span>
        )}
        <br />
        <button type="submit">{t('customerForm.submit')}</button>
      </form>
    </div>
  );
};

export default UpdateCustomerForm;
