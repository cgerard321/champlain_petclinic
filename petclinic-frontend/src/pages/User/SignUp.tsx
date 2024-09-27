import * as React from 'react';
import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { OwnerModel } from '@/features/customers/models/OwnerModel.ts';
import { UserIdLessRoleLessDTO } from '@/shared/models/UserIdLessRoleLessDTO';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axios from 'axios';
import './SignUp.css';

const SignUp: React.FC = (): JSX.Element => {
  //This is the character limit for all fields but the email (Since a valid email can go up to 320 characters)
  const characterLimit = 60;
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

  const [userData, setUserData] = useState<UserIdLessRoleLessDTO>({
    userId: '',
    username: '',
    password: '',
    email: '',
    defaultRole: '',
  });

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
    if (email.length > 320) return 'Password cannot exceed 320 characters.';
    if (!emailRegex.test(email)) {
      return 'Invalid email format.';
    }
    return undefined;
  };

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

  //To instantly notify the user when they are inputing the fields
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

    // Validate all fields when submitting (Since they all have individual validation)
    const errors: Partial<Record<string, string>> = {};
    Object.keys(owner).forEach(key => {
      const error = validateField(
        key,
        owner[key as keyof OwnerModel] as string
      );
      if (error) {
        errors[key] = error;
        hasErrors = true;
      }
    });

    Object.keys(userData).forEach(key => {
      const error = validateField(
        key,
        userData[key as keyof UserIdLessRoleLessDTO] as string
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

    // Submission (Currently using old endpoint)
    try {
      const response = await axios.post<UserIdLessRoleLessDTO>(
        'http://localhost:8080/api/gateway/users',
        userData
      );

      if (response.data.userId !== '') {
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
          // For error statuses or unknown errors
          setErrorMessage({
            general: 'Something went wrong, please try again later.',
          });
        }
      } else {
        // For Non-Axios errors
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
