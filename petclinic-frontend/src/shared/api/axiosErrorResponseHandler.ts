import { AxiosError } from 'axios';
import router from '@/router';

// Map status codes to their respective error pages
const errorPageRedirects: Record<number, string> = {
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
    console.error('Unauthorized access. Clearing credentials and redirecting to home.');
    localStorage.clear();
    // Use the router here for a clean client-side navigation
    router.navigate('/home');
    return;
  }

  const redirectPath = errorPageRedirects[statusCode];

  if (redirectPath) {
    console.error(`Redirecting to ${redirectPath} due to a server error:`, error);
    // Use window.location for hard redirects on critical errors
    if (typeof window !== 'undefined') {
      window.location.href = redirectPath;
    }
  } else {
    // Log any unhandled global errors
    console.error('Unhandled global error:', error, 'Status code:', statusCode);
  }
}