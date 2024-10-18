import * as React from 'react';
import { FormEvent, useEffect, useState, FC } from 'react';
import { updateUser, getUser } from '@/features/users/api/updateUser';
import { UserPasswordLessDTO } from '@/features/users/model/UserPasswordLessDTO';
import { useNavigate, useParams } from 'react-router-dom';
import { AxiosResponse } from 'axios';
import './UpdateUserForm.css';

const UpdateUserForm: FC = (): JSX.Element => {
  const navigate = useNavigate();
  const { userId } = useParams<{ userId: string }>();

  const [userData, setUserData] = useState<UserPasswordLessDTO>({
    userId: userId || '',
    username: '',
    email: '',
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const characterLimit = 60;

  const validateUsername = (username: string): string | undefined => {
    if (!username) return 'Username is required.';
    if (username.length > characterLimit)
      return 'Username cannot exceed 60 characters.';
    return undefined;
  };

  const validateEmail = (email: string): string | undefined => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) return 'Email is required.';
    if (email.length > 320) return 'Email cannot exceed 320 characters.';
    if (!emailRegex.test(email)) return 'Invalid email format.';
    return undefined;
  };

  const validateField = (name: string, value: string): string | undefined => {
    switch (name) {
      case 'username':
        return validateUsername(value);
      case 'email':
        return validateEmail(value);
      default:
        return undefined;
    }
  };

  useEffect(() => {
    const fetchUserData = async (): Promise<void> => {
      try {
        const response = await getUser(userId!);
        const userResponse: UserPasswordLessDTO = response.data;
        setUserData({ ...userResponse });
      } catch (error) {
        console.error('Error fetching user data:', error);
        setSubmitError('Failed to fetch user data. Please try again later.');
      }
    };

    fetchUserData().catch(error =>
      console.error('Error in fetchUserData:', error)
    );
  }, [userId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    setUserData({ ...userData, [name]: value });
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    Object.keys(userData).forEach(field => {
      const error = validateField(
        field,
        userData[field as keyof UserPasswordLessDTO]
      );
      if (error) newErrors[field] = error;
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    setSubmitError(null);

    if (!validate()) return;

    try {
      const response = await updateUser(userId!, userData);
      if (response.status === 200) {
        setIsModalOpen(true);
      } else {
        handleResponseErrors(response);
      }
    } catch (error) {
      console.error('Error:', error);
      setSubmitError(
        'An error occurred while updating the user. Please try again later.'
      );
    }
  };

  const handleResponseErrors = (response: AxiosResponse): void => {
    if (response.status === 400) {
      setSubmitError('Invalid data provided. Please check your input.');
    } else if (response.status === 404) {
      setSubmitError('User not found. Please refresh and try again.');
    } else {
      setSubmitError('An unexpected error occurred. Please try again later.');
    }
  };

  const closeModal = (): void => {
    setIsModalOpen(false);
    navigate(`/users/${userId}`);
  };

  return (
    <div className="update-user-form">
      <h1>Edit User Profile</h1>
      {submitError && <div className="error-message">{submitError}</div>}
      <form onSubmit={handleSubmit}>
        <label>Username: </label>
        <input
          type="text"
          name="username"
          value={userData.username}
          onChange={handleChange}
        />
        {errors.username && <span className="error">{errors.username}</span>}
        <br />
        <label>Email: </label>
        <input
          type="email"
          name="email"
          value={userData.email}
          onChange={handleChange}
        />
        {errors.email && <span className="error">{errors.email}</span>}
        <br />
        <button type="submit">Update</button>
      </form>

      {isModalOpen && (
        <div className="admin-update-user-modal-overlay">
          <div className="admin-update-user-modal">
            <h2>Success!</h2>
            <p>User has been successfully updated.</p>
            <button onClick={closeModal}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UpdateUserForm;
