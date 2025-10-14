import { AxiosError } from 'axios';
import router from '@/router';

// map status codes to error pages
const errorPageRedirects: Record<number, string> = {
  401: '/unauthorized', // redirect to unauthorized
  403: '/forbidden', // redirect to forbidden
  408: '/request-timeout', // redirect to request
  500: '/internal-server-error', // redirect to internal server
  503: '/service-unavailable', // redirect to service unavailable
};

// handles error and redirects based on status codes
export default function axiosErrorResponseHandler(
  error: AxiosError,
  statusCode: number
): void {
  const redirectPath = errorPageRedirects[statusCode];
  if (statusCode == 401) {
    localStorage.clear();
    router.navigate('/home');
  }
  if (redirectPath) {
    // log for easy debug
    console.error(`Redirecting to ${redirectPath} due to error:`, error);
    router.navigate(redirectPath);
  } else {
    // log whatever that wasn't handled
    console.error('Unhandled error:', error, 'Status code:', statusCode);
  }
}
