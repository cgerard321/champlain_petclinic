import axios, { AxiosInstance } from 'axios';
import axiosErrorResponseHandler from '@/shared/api/axiosErrorResponseHandler.ts';

axios.defaults.withCredentials = true;

const createAxiosInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // response interceptor to handle errors globally
  instance.interceptors.response.use(
    response => response,
    error => {
      // handle errors through a custom error handler
      handleAxiosError(error);
      // yes, now try-catch can actually catch the error unlike before :D
      return Promise.reject(error);
    }
  );

  return instance;
};

const handleAxiosError = (error: unknown): void => {
  // check if is Axios error
  if (axios.isAxiosError(error)) {
    // get status code from error
    const statusCode = error.response?.status ?? 0;
    // call error response handler
    axiosErrorResponseHandler(error, statusCode);
  } else {
    // log other errors that are not Axios errors
    console.error('An unexpected error occurred:', error);
  }
};

const axiosInstance = createAxiosInstance();
export default axiosInstance;
