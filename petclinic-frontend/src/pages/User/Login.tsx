import { useState, FormEvent } from 'react';
import axios from 'axios';
import { UserResponseModel } from '@/shared/models/UserResponseModel';
import { useSetUser } from '@/context/UserContext.tsx';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

export default function Login(): JSX.Element {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const setUser = useSetUser();
  const navigate = useNavigate();
  const login = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    setErrorMessage(null);

    const form = event.currentTarget;
    const formElements = form.elements as typeof form.elements & {
      emailInput: HTMLInputElement;
      passwordInput: HTMLInputElement;
    };

    try {
      const response = await axios.post<UserResponseModel>(
        'http://localhost:8080/api/gateway/users/login',
        {
          email: formElements.emailInput.value,
          password: formElements.passwordInput.value,
        }
      );

      setUser(response.data);
      if (response.data.userId !== '') {
        navigate(AppRoutePaths.Home);
      }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        setErrorMessage('Invalid email or password. Please try again.');
      } else {
        setErrorMessage('Something went wrong, oops!');
      }
    }
  };

  return (
    <div>
      <h1>User Login</h1>
      {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
      <form onSubmit={login}>
        <label>Email: </label>
        <input type="text" id="emailInput" />
        <br />
        <label>Password: </label>
        <input type="password" id="passwordInput" />
        <br />
        <button type="submit">Login</button>
      </form>
    </div>
  );
}