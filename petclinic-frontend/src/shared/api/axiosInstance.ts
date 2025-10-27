// src/shared/api/axiosInstance.ts

import axios, {
  AxiosError,
  AxiosInstance,
  InternalAxiosRequestConfig,
} from 'axios';
import axiosErrorResponseHandler from '@/shared/api/axiosErrorResponseHandler.ts';

axios.defaults.withCredentials = true;

// Extend AxiosRequestConfig to include our custom properties
declare module 'axios' {
  export interface AxiosRequestConfig {
    useV2?: boolean;
    handleLocally?: boolean; // <-- Add the new optional property
  }
}

// Extend InternalAxiosRequestConfig as well
interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  useV2?: boolean;
  handleLocally?: boolean; // <-- Add the new optional property
}

const createAxiosInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  });
  

  // Request interceptor (no changes needed here)
  instance.interceptors.request.use(
    (config: CustomAxiosRequestConfig) => {
      const useV2 = config.useV2 !== undefined ? config.useV2 : true;
      const versionPath = useV2 ? '/v2/gateway' : '/gateway';

      if (config.url && !config.url.startsWith('http')) {
        config.url = versionPath + config.url;
      }

      // We don't delete `handleLocally` here because the error handler needs it later.
      delete config.useV2;

      return config;
    },
    error => Promise.reject(error)
  );

  // Response interceptor to handle errors globally
  instance.interceptors.response.use(
    response => response,
    error => {
      const shouldStopPropagation = handleAxiosError(error);

      if (shouldStopPropagation) {
        return Promise.resolve({ data: null, status: error.response?.status });
      }

      return Promise.reject(error);
    }
  );

  return instance;
};

const handleAxiosError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    // get status code from error
    const statusCode = error.response?.status ?? 0;
    const config = error.config as CustomAxiosRequestConfig;

    const globallyHandledCodes = [401, 403, 500, 502, 503, 504];

    if (globallyHandledCodes.includes(statusCode)) {
      if (config?.handleLocally) {
        return false; // Let the component's catch block handle it
      }

      // If no bypass is requested, proceed with global handling.
      axiosErrorResponseHandler(error as AxiosError, statusCode);
      return true;
    }

    // For all other errors, allow local handling as before
    return false;
  }

  console.error('An unexpected non-Axios error occurred:', error);
  return false;
};

const axiosInstance = createAxiosInstance();

export default axiosInstance;
