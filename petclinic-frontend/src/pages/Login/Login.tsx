import axiosInstance from '@/shared/api/axiosInstance';
import { UserResponseModel } from '@/shared/models/UserResponseModel';
import { FormEvent } from 'react';
import { useSetUser } from '@/context/UserContext.tsx';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

export default function Login(): JSX.Element {
  const setUser = useSetUser();
  const navigate = useNavigate();
  const login = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    const form = event.currentTarget;
    const formElements = form.elements as typeof form.elements & {
      emailInput: HTMLInputElement;
      passwordInput: HTMLInputElement;
    };
    await axiosInstance
      .post<UserResponseModel>(
        'http://localhost:8080/api/gateway/' + 'users/login',
        {
          email: formElements.emailInput.value,
          password: formElements.passwordInput.value,
        }
      )
      .then(response => {
        setUser(response.data);
        if (response.data.userId !== '') {
          navigate(AppRoutePaths.Default);
        }
      });
  };
  return (
    <div>
      <h1>User Login</h1>
      <form onSubmit={login}>
        <label>Email: </label>
        <input type="text" id="emailInput" />
        <br />
        <label>Password: </label>
        <input type="text" id="passwordInput" />
        <br />
        <button type="submit">Login</button>
      </form>
    </div>
  );
}
