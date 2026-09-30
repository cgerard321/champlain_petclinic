import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { extractApiError } from '@core/models/api-error';

export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((error: unknown) => {
      const backendError = extractApiError(error);
      return throwError(() => backendError ?? error);
    }),
  );
