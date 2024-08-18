import { AxiosError } from 'axios';
import router from '@/router';

export default function axiosErrorResponseHandler(
  error: AxiosError,
  statusCode: number
): void {
  switch (statusCode) {
    case 401:
      // redirrect to the unauthorized page
      router.navigate('/unauthorized');
      break;
    case 403:
      // redirrect to the forbidden page
      router.navigate('/unauthorized');
      break;
    case 408:
      // redirrect to the request timeout page
      router.navigate('/request-timeout');
      break;
    case 500:
      // redirrect to the internal server error page
      router.navigate('/internal-server-error');
      break;
    case 503:
      // redirrect to the service unavailable page
      router.navigate('/service-unavailable');
      break;
    default:
      console.error(error, statusCode);
  }
}
