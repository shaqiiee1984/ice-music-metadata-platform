import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('ice_auth_token');
  const correlationId = 'web-' + Math.random().toString(36).substring(2, 11);

  let headers = req.headers.set('X-Correlation-ID', correlationId);

  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  const cloned = req.clone({ headers });
  return next(cloned);
};
