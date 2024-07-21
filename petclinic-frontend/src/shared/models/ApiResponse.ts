import { DisplayMessageUI } from '@/shared/models/DisplayMessageUI.ts';

export interface ApiResponse<T> {
  message: string;
  displayMessageUI: DisplayMessageUI;
  data: T | null;
}
