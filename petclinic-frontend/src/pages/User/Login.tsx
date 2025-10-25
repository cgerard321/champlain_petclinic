import { useState, FormEvent } from 'react';
import { UserResponseModel } from '@/shared/models/UserResponseModel';
import { useSetUser } from '@/context/UserContext.tsx';
import axiosInstance from '@/shared/api/axiosInstance';
import { Link, useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import anotherDoctorAndDoggy from '@/assets/Login/another-doctor-and-doggy.jpg';
import doctorAndDoggy from '@/assets/Login/doctor-and-doggy.jpg';
import doggyAndKitty from '@/assets/Login/doggy-and-kitty.jpg';
import kitty from '@/assets/Login/kitty.jpg';
import sadDoggy from '@/assets/Login/sad-doggy.jpg';
import './Login.css';
import Slideshow from './Slideshow';
import { Alert } from 'react-bootstrap';
import { isAxiosError } from 'axios';
import SvgIcon from '@/shared/components/SvgIcon';
const images = [
  doctorAndDoggy,
  anotherDoctorAndDoggy,
  doggyAndKitty,
  kitty,
  sadDoggy,
];

export default function Login(): JSX.Element {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const setUser = useSetUser();
  const navigate = useNavigate();
  function togglePasswordVisibility(): void {
    const passwordInput = document.getElementById(
      'passwordInput'
    ) as HTMLInputElement;
    if (passwordInput.type === 'password') {
      passwordInput.type = 'text';
    } else {
      passwordInput.type = 'password';
    }
  }
  const login = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    setErrorMessage(null);

    const form = event.currentTarget;
    const formElements = form.elements as typeof form.elements & {
      emailInput: HTMLInputElement;
      passwordInput: HTMLInputElement;
    };

    // the try catch is being overwritten by the redirect to /unauthorized so exception handling isnt doing anything for now
    try {
      const response = await axiosInstance.post<UserResponseModel>(
        '/users/login',
        {
          email: formElements.emailInput.value,
          password: formElements.passwordInput.value,
        },
        {
          useV2: false,
          handleLocally: true,
        }
      );

      setUser(response.data);
      if (response.data.userId !== '') {
        navigate(AppRoutePaths.Home);
      }
    } catch (error) {
      if (isAxiosError(error)) {
        if (error.response?.status === 401) {
          const errorMessage = error.response?.data?.message;
          // Check for the custom unverified account error
          if (
            errorMessage ===
            'Your account is not verified ! A link has been sent to verify the account !'
          ) {
            setErrorMessage(errorMessage);
          } else if (
            errorMessage ===
            'Your account has been disabled. Please contact support.'
          ) {
            setErrorMessage(
              'Your account has been disabled. Please contact support.'
            );
          } else {
            // Handle invalid credentials
            setErrorMessage('Invalid email or password. Please try again.');
          }
        } else {
          // Handle general error scenario
          setErrorMessage('Something went wrong, oops!');
        }
      } else {
        setErrorMessage('An unknown error occurred.');
      }
    }
  };

  return (
    <div className="login-page">
      <Link to="/home" className="back-button">
        Back
      </Link>
      <div className="login-container">
        <h1>User Login</h1>
        {errorMessage && <Alert variant="danger">{errorMessage}</Alert>}
        <form onSubmit={login}>
          <label htmlFor="emailInput"></label>
          <input
            type="text"
            id="emailInput"
            placeholder="Enter your email or username"
          />
          <br />
          <label htmlFor="passwordInput"></label>
          <div className="input-wrapper">
            <input
              type="password"
              id="passwordInput"
              placeholder="Enter your password"
            />
            <button
              className="toggle-password"
              id="toggle-password"
              onClick={togglePasswordVisibility}
              type="button"
              title="Show Password"
            >
              <SvgIcon id="eye" />
            </button>
          </div>
          <br />
          <button type="submit" className="login-button">
            Login
          </button>
        </form>
        <div className="signup-button-container">
          <Link to={AppRoutePaths.ForgotPassword} className="link">
            Forgot Password
          </Link>
          <hr className="separator" />
          <Link to={AppRoutePaths.SignUp} className="link">
            Sign Up
          </Link>
        </div>
      </div>
      <Slideshow images={images} interval={7000} />
    </div>
  );
}
