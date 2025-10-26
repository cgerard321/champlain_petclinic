import * as React from 'react';
import { FormEvent, useState } from 'react';
import { OwnerRequestModel } from '@/shared/models/OwnerRequestModel';
import { Register } from '@/shared/models/RegisterModel';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axiosInstance from '@/shared/api/axiosInstance';
import { isAxiosError } from 'axios';
import './SignUp.css';
import SvgIcon from '@/shared/components/SvgIcon';

const SignUp: React.FC = (): JSX.Element => {
  const characterLimit = 60;
  const [errorMessage, setErrorMessage] = useState<
    Partial<Record<string, string>>
  >({});
  const [loading, setLoading] = useState(false);
  const [emailSent, setEmailSent] = useState(false);

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

  const [showPassword, setShowPassword] = useState(false);

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
    if (!emailRegex.test(email)) return 'Invalid email format.';
    return undefined;
  };

  const validatePhoneNumber = (phone: string): string | undefined => {
    const phoneRegex = /^[0-9]{10}$/;
    if (!phone) return 'Telephone number is required.';
    if (!phoneRegex.test(phone))
      return 'Invalid phone number format. Please enter a 10-digit number.';
    return undefined;
  };

  const checkLength = (
    field: string,
    value: string,
    maxLength: number
  ): string | undefined => {
    if (value.length > maxLength)
      return `${field} cannot exceed ${maxLength} characters.`;
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

  const validateField = (name: string, value: string): string | undefined => {
    const label = fieldLabels[name] || name;
    switch (name) {
      case 'firstName':
      case 'lastName':
      case 'address':
      case 'city':
      case 'province':
      case 'username':
        if (!value) return `${label} is required.`;
        return checkLength(label, value, characterLimit);
      case 'telephone':
        return validatePhoneNumber(value);
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
    setLoading(true);

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
      setLoading(false);
      return;
    }

    const requestData = { ...userData, owner };

    try {
      //using the axios instance
      const response = await axiosInstance.post<Register>(
        '/users',
        requestData,
        { useV2: false }
      );

      if (response.status === 201) {
        setEmailSent(true);
      }
    } catch (error) {
      setLoading(false);
      if (isAxiosError(error) && error.response) {
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
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <NavBar />
      <div className="signup-user-form">
        {loading ? (
          <div className="loading-icon">Loading...</div>
        ) : emailSent ? (
          <div className="success-message">
            <h2>Email Sent!</h2>
            <p>
              A confirmation email has been sent to{' '}
              <strong>{userData.email}</strong>.
            </p>
            <p>
              Please check your inbox and follow the instructions in the email
              to complete your registration.
            </p>
            <p>Make sure to check the spam folder!</p>
            <p>No emails received? Please contact us at (xxx)-xxxx-xxxx</p>
          </div>
        ) : (
          <>
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
              <div className="input-wrapper">
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={userData.password}
                  onChange={handleChange}
                  className="password-container"
                />
                <button
                  className="toggle-password"
                  id="toggle-password"
                  onClick={() => setShowPassword(prev => !prev)}
                  type="button"
                  title="Show Password"
                >
                  <SvgIcon id="eye" />
                </button>
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
          </>
        )}
      </div>
    </div>
  );
};

export default SignUp;
