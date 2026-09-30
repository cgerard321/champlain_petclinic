import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
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

const UpdateCustomerForm: React.FC = (): JSX.Element => {
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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
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
    if (!customer.firstName) newErrors.firstName = 'First name is required';
    if (!customer.lastName) newErrors.lastName = 'Last name is required';
    if (!customer.address) newErrors.address = 'Address is required';
    if (!customer.city) newErrors.city = 'City is required';
    if (!customer.province) newErrors.province = 'Province is required';
    if (!customer.telephone) newErrors.telephone = 'Telephone is required';

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

      navigate(AppRoutePaths.Home);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="update-customer-form">
      <h1>Edit Profile</h1>
      <form onSubmit={handleSubmit}>
        <label>Username: </label>
        <input
          type="text"
          name="username"
          value={username}
          onChange={handleUsernameChange}
        />
        {errors.username && <span className="error">{errors.username}</span>}
        <br />
        <label>First Name: </label>
        <input
          type="text"
          name="firstName"
          value={customer.firstName}
          onChange={handleChange}
        />
        {errors.firstName && <span className="error">{errors.firstName}</span>}
        <br />
        <label>Last Name: </label>
        <input
          type="text"
          name="lastName"
          value={customer.lastName}
          onChange={handleChange}
        />
        {errors.lastName && <span className="error">{errors.lastName}</span>}
        <br />
        <label>Address: </label>
        <input
          type="text"
          name="address"
          value={customer.address}
          onChange={handleChange}
        />
        {errors.address && <span className="error">{errors.address}</span>}
        <br />
        <label>City: </label>
        <input
          type="text"
          name="city"
          value={customer.city}
          onChange={handleChange}
        />
        {errors.city && <span className="error">{errors.city}</span>}
        <br />
        <label>Province: </label>
        <input
          type="text"
          name="province"
          value={customer.province}
          onChange={handleChange}
        />
        {errors.province && <span className="error">{errors.province}</span>}
        <br />
        <label>Telephone: </label>
        <input
          type="text"
          name="telephone"
          value={customer.telephone}
          onChange={handleChange}
        />
        {errors.telephone && <span className="error">{errors.telephone}</span>}
        <br />
        <button type="submit">Update</button>
      </form>
    </div>
  );
};

export default UpdateCustomerForm;
