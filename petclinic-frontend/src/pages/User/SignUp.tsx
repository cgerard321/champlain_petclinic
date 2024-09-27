import * as React from 'react';
import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { OwnerRequestModel } from '@/shared/models/OwnerRequestModel';
import { Register } from '@/shared/models/RegisterModel';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axios from 'axios';
import './SignUp.css';

const SignUp: React.FC = (): JSX.Element => {
  // Character limit for all fields but email (Since a valid email can go up to 320 characters)
  const characterLimit = 60;
  const [errorMessage, setErrorMessage] = useState<
    Partial<Record<string, string>>
  >({});
  const navigate = useNavigate();
  //Models needed to communicate with the backend
  const [owner, setOwner] = useState<OwnerRequestModel>({
    ownerId: '',
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  });

  const [userData, setUserData] = useState<Register>({
    userId: '',
    email: '',
    username: '',
    password: '',
    defaultRole: '',
    owner,
  });

  //Made difference functions for password and email since they require more checks
  const validatePassword = (password: string): string | undefined => {
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/;
    if (!password) return 'Password is required.';
    if (password.length > characterLimit)
      return 'Password cannot exceed 60 characters.';
    if (!passwordRegex.test(password)) {
      return 'Password must contain at least 8 characters, including an uppercase letter, a lowercase letter, a number, and a special character.';
    }
    return undefined;
  };

  const validateEmail = (email: string): string | undefined => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) return 'Email is required.';
    if (email.length > 320) return 'Email cannot exceed 320 characters.';
    if (!emailRegex.test(email)) {
      return 'Invalid email format.';
    }
    return undefined;
  };

  //Since every field has a limit and needs to be checked indepedently, I made this function to quickly check any length and if it is valid or not
  const checkLength = (
    field: string,
    value: string,
    maxLength: number
  ): string | undefined => {
    if (value.length > maxLength) {
      return `${field} cannot exceed ${maxLength} characters.`;
    }
    return undefined;
  };

  //Just to make validation look better for users
  const fieldLabels: Record<string, string> = {
    firstName: 'First Name',
    lastName: 'Last Name',
    address: 'Address',
    city: 'City',
    province: 'Province',
    telephone: 'Telephone',
    username: 'Username',
    password: 'Password',
    email: 'Email',
  };

  //Check individually to give more reactive feedback to the user
  const validateField = (name: string, value: string): string | undefined => {
    const label = fieldLabels[name] || name;

    switch (name) {
      case 'firstName':
      case 'lastName':
      case 'address':
      case 'city':
      case 'province':
      case 'telephone':
      case 'username':
        if (!value) return `${label} is required.`;
        return checkLength(label, value, characterLimit);
      case 'password':
        return validatePassword(value);
      case 'email':
        return validateEmail(value);
      default:
        return undefined;
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;

    if (name in owner) {
      setOwner({ ...owner, [name]: value });
    } else if (name in userData) {
      setUserData({ ...userData, [name]: value });
    }

    const error = validateField(name, value);
    setErrorMessage(prev => ({ ...prev, [name]: error ?? undefined }));
  };

  const signup = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    setErrorMessage({});
    let hasErrors = false;

    // Validates all fields before submitting form
    const errors: Partial<Record<string, string>> = {};
    Object.keys(owner).forEach(key => {
      const error = validateField(
        key,
        owner[key as keyof OwnerRequestModel] as string
      );
      if (error) {
        errors[key] = error;
        hasErrors = true;
      }
    });

    Object.keys(userData).forEach(key => {
      const error = validateField(
        key,
        userData[key as keyof Register] as string
      );
      if (error) {
        errors[key] = error;
        hasErrors = true;
      }
    });

    setErrorMessage(errors);

    if (hasErrors) {
      return;
    }

    // Preparing the data to send to the backend
    const requestData = {
      ...userData,
      owner,
    };

    // Submission function
    try {
      const response = await axios.post<Register>(
        'http://localhost:8080/api/gateway/users',
        requestData
      );

      if (response.data.userId) {
        navigate(AppRoutePaths.Home);
      }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        const backendErrors = error.response.data;

        if (error.response.status === 400) {
          const errorMessageString =
            backendErrors.message ||
            'Invalid input. Please check the fields and try again.';
          setErrorMessage({ general: errorMessageString });
        } else {
          setErrorMessage({
            general: 'Something went wrong, please try again later.',
          });
        }
      } else {
        setErrorMessage({
          general: 'An unexpected error occurred. Please try again.',
        });
      }
    }
  };

  return (
    <div>
      <NavBar />
      <div className="signup-user-form">
        <h1>Create New User</h1>
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
          <label>Password: </label>
          <div className="password-container">
            <input
              type="password"
              name="password"
              value={userData.password}
              onChange={handleChange}
            />
          </div>
          {errorMessage.password && (
            <span className="error">{errorMessage.password}</span>
          )}
          <br />
          {errorMessage.general && (
            <span className="error">{errorMessage.general}</span>
          )}
          <button type="submit" className="submit-button">
            Send Verification Email
          </button>
        </form>
      </div>
    </div>
  );
};

export default SignUp;
