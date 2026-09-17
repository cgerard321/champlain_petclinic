import { HttpErrorResponse } from '@angular/common/http';

export interface ApiError {
  code: number | string;
  message: string;
}

export function isApiError(value: unknown): value is ApiError {
  return typeof value === 'object' && value !== null && 'message' in value && 'code' in value;
}

export function extractApiError(err: unknown): ApiError | null {
  if (err instanceof HttpErrorResponse && typeof err.error?.message === 'string') {
    return { code: err.error.reason ?? err.status, message: err.error.message };
  }

  return null;
}
