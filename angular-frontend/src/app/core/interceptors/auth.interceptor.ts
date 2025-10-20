import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // JWT token is handled by cookies automatically by the browser
  // No need to manually add Authorization headers
  return next(req);
};
