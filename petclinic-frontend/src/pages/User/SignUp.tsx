import * as React from 'react';
import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import '@/features/customers/components/UpdateCustomerForm.css';
import { OwnerModel } from '@/features/customers/models/OwnerModel.ts';
import { UserIdLessRoleLessDTO } from '@/shared/models/UserIdLessRoleLessDTO';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axios from 'axios';
import './SignUp.css';

const SignUp: React.FC = (): JSX.Element => {
  const [errorMessage, setErrorMessage] = useState<
    Partial<Record<string, string>>
  >({});
  const navigate = useNavigate();

  const [owner, setOwner] = useState<OwnerModel>({
    ownerId: '',
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
    pets: [],
  });

  // State for user data
  const [userData, setUserData] = useState<UserIdLessRoleLessDTO>({
    userId: '',
    username: '',
    password: '',
    email: '',
    defaultRole: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;

    if (name in owner) {
      setOwner({ ...owner, [name]: value });
      setErrorMessage(prev => ({ ...prev, [name]: undefined }));
    } else if (name in userData) {
      setUserData({ ...userData, [name]: value });
      setErrorMessage(prev => ({ ...prev, [name]: undefined }));
    }
  };

  const validateFields = (): boolean => {
    const errors: Partial<Record<string, string>> = {};
    if (!owner.firstName) errors.firstName = 'First name is required.';
    if (!owner.lastName) errors.lastName = 'Last name is required.';
    if (!owner.address) errors.address = 'Address is required.';
    if (!owner.city) errors.city = 'City is required.';
    if (!owner.province) errors.province = 'Province is required.';
    if (!owner.telephone) errors.telephone = 'Telephone is required.';
    if (!userData.username) errors.username = 'Username is required.';
    if (!userData.password) errors.password = 'Password is required.';
    if (!userData.email) errors.email = 'Email is required.';

    setErrorMessage(errors);
    return Object.keys(errors).length === 0;
  };

  const signup = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    setErrorMessage({});

    if (!validateFields()) {
      return;
    }

    try {
      const response = await axios.post<UserIdLessRoleLessDTO>(
        'http://localhost:8080/api/gateway/users',
        userData
      );

      if (response.data.userId !== '') {
        navigate(AppRoutePaths.Home);
      }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 400) {
        setErrorMessage({
          general:
            'Invalid input. Please check the fields and try again.' + error,
        });
      } else {
        setErrorMessage({ general: 'Something went wrong, oops!' });
      }
    }
  };

  return (
    <div>
      <NavBar />
      <div className="add-customer-form">
        <h1>Add Customer</h1>
        <form onSubmit={signup}>
          <label>First Name: </label>
          <input
            type="text"
            name="firstName"
            value={owner.firstName}
            onChange={handleChange}
          />
          {errorMessage.firstName && (
            <span className="error">{errorMessage.firstName}</span>
          )}
          <br />
          <label>Last Name: </label>
          <input
            type="text"
            name="lastName"
            value={owner.lastName}
            onChange={handleChange}
          />
          {errorMessage.lastName && (
            <span className="error">{errorMessage.lastName}</span>
          )}
          <br />
          <label>Address: </label>
          <input
            type="text"
            name="address"
            value={owner.address}
            onChange={handleChange}
          />
          {errorMessage.address && (
            <span className="error">{errorMessage.address}</span>
          )}
          <br />
          <label>City: </label>
          <input
            type="text"
            name="city"
            value={owner.city}
            onChange={handleChange}
          />
          {errorMessage.city && (
            <span className="error">{errorMessage.city}</span>
          )}
          <br />
          <label>Province: </label>
          <input
            type="text"
            name="province"
            value={owner.province}
            onChange={handleChange}
          />
          {errorMessage.province && (
            <span className="error">{errorMessage.province}</span>
          )}
          <br />
          <label>Telephone: </label>
          <input
            type="text"
            name="telephone"
            value={owner.telephone}
            onChange={handleChange}
          />
          {errorMessage.telephone && (
            <span className="error">{errorMessage.telephone}</span>
          )}
          <br />
          <label>Username: </label>
          <input
            type="text"
            name="username"
            value={userData.username}
            onChange={handleChange}
          />
          {errorMessage.username && (
            <span className="error">{errorMessage.username}</span>
          )}
          <br />
          <label>Password: </label>
          <input
            type="password"
            name="password"
            value={userData.password}
            onChange={handleChange}
          />
          {errorMessage.password && (
            <span className="error">{errorMessage.password}</span>
          )}
          <br />
          <label>Email: </label>
          <input
            type="email"
            name="email"
            value={userData.email}
            onChange={handleChange}
          />
          {errorMessage.email && (
            <span className="error">{errorMessage.email}</span>
          )}
          <br />
          {errorMessage.general && (
            <span className="error">{errorMessage.general}</span>
          )}
          <button type="submit">Send Verification Email</button>
        </form>
      </div>
    </div>
  );
};

export default SignUp;
