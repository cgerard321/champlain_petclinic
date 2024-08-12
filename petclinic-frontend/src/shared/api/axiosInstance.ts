import axios, { AxiosError, AxiosInstance } from 'axios';
import { ApiResponse } from '@/shared/models/ApiResponse.ts';
import axiosErrorResponseHandler from '@/shared/api/axiosErrorResponseHandler.ts';

axios.defaults.withCredentials = true;



const axiosInstance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// axiosInstance.interceptors.request.use(
//   async config => {
//     const token = 'Bearer '; //get id token
//     config.headers.Authorization = token;
//     return config;
//   },
//   error => {
//     return error;
//   }
// );

axiosInstance.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (axios.isAxiosError(error)) {
      const response = error.response?.data as ApiResponse<AxiosError>;
      axiosErrorResponseHandler(response, error.response?.status ?? 0);
      // call the api error response handler
      return Promise.reject(response); // this is temporary until we have a our global axios error handler that redirects to the correct pages for the responses.
    } else {
      return Promise.reject(error as Error);
    }
  }
);

export default axiosInstance;
