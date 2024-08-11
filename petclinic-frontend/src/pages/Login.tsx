import axiosInstance from '@/shared/api/axiosInstance';
import { ApiResponse } from '@/shared/models/ApiResponse';
import { UserResponseModel } from '@/shared/models/UserResponseModel';
import { FormEvent } from 'react';

export default function Login(): JSX.Element {
  const login = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    const form = event.currentTarget;
    const formElements = form.elements as typeof form.elements & {
      emailInput: HTMLInputElement;
      passwordInput: HTMLInputElement;
    };
    const userData: UserResponseModel = await axiosInstance.post<
      ApiResponse<UserResponseModel>
    >(axiosInstance.defaults.baseURL + 'users/login', {
      email: formElements.emailInput.value,
      password: formElements.passwordInput.value,
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
