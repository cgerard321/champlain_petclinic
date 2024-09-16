import { AxiosError } from 'axios';
import router from '@/router';

export default function axiosErrorResponseHandler(
  error: AxiosError,
  statusCode: number
): void {
  switch (statusCode) {
    case 401:
      // redirect to the unauthorized page
      router.navigate('/unauthorized');
      break;
    case 403:
      // redirect to the forbidden page
      router.navigate('/forbidden');
      break;
    case 408:
      // redirect to the request timeout page
      router.navigate('/request-timeout');
      break;
    case 500:
      // redirect to the internal server error page
      router.navigate('/internal-server-error');
      break;
    case 503:
      // redirect to the service unavailable page
      router.navigate('/service-unavailable');
      break;
    default:
      console.error(error, statusCode);
  }
}
