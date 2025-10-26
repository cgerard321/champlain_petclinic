import { AxiosError } from 'axios';
import axiosInstance from "@/shared/api/axiosInstance.ts";

// Map status codes to their respective error pages
const errorPageRedirects: Record<number, string> = {
  401: '/unauthorized',
  403: '/forbidden',
  408: '/request-timeout',
  500: '/internal-server-error',
  503: '/service-unavailable',
};

// Handles redirection for globally managed errors
export default function axiosErrorResponseHandler(
  error: AxiosError,
  statusCode: number
): void {
  // Specific handling for 401 Unauthorized
  if (statusCode === 401) {
      const wasLoggedIn = localStorage.getItem('user') !== null;

      if (wasLoggedIn) {
          console.error('Session expired. Logging out and redirecting to unauthorized page.');

          axiosInstance.post('/users/logout', {}).catch(() => {
          });

          localStorage.clear();

          sessionStorage.setItem('sessionExpired', 'true');
      } else {
          console.error('Unauthorized access detected.');
      }
  }

  const redirectPath = errorPageRedirects[statusCode];

  if (redirectPath) {
    console.error(
      `Redirecting to ${redirectPath} due to a server error:`,
      error
    );
    // Use window.location for hard redirects on critical errors
    if (typeof window !== 'undefined') {
      window.location.href = redirectPath;
    }
  } else {
    // Log any unhandled global errors
    console.error('Unhandled global error:', error, 'Status code:', statusCode);
  }
}
