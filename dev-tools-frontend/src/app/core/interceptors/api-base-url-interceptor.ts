import { HttpInterceptorFn } from '@angular/common/http';

import { environment } from '../../../environments/environment';

export const apiBaseUrlInterceptor: HttpInterceptorFn = (req, next) => {
  const isRelative = !/^https?:\/\//i.test(req.url);

  if (!isRelative) {
    return next(req);
  }

  return next(req.clone({ url: `${environment.apiUrl}/api/v1${req.url}` }));
};
