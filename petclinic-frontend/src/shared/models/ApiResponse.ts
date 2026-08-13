export interface ApiResponse<T> {
  data: T | null;
  errorMessage: string | null;
}
