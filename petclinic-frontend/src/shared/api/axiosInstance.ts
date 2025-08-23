import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import axiosErrorResponseHandler from '@/shared/api/axiosErrorResponseHandler.ts';

axios.defaults.withCredentials = true;

// Extend InternalAxiosRequestConfig to include our custom useV2 property
interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  useV2?: boolean;
}

const createAxiosInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL, // Base URL without version
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Request interceptor to modify URL based on useV2 flag
  instance.interceptors.request.use(
    (config: CustomAxiosRequestConfig) => {
      // Default to v2 if useV2 is not specified
      const useV2 = config.useV2 !== undefined ? config.useV2 : true;
      const versionPath = useV2 ? '/v2/gateway/' : '/v1/gateway/';

      // Modify the URL to include the version path
      if (
        config.url &&
        !config.url.startsWith('http://') &&
        !config.url.startsWith('https://')
      ) {
        config.url = versionPath + config.url;
      }

      // Remove the custom property from config
      delete config.useV2;

      return config;
    },
    error => {
      return Promise.reject(error);
    }
  );

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
