import { PredefinedDescription } from '@/features/veterinarians/models/PredefinedDescription';

export interface VetRatingRequestModel {
  vetId: string;
  rateScore: number;
  rateDescription: string;
  rateDate?: string;
  predefinedDescription?: PredefinedDescription | null;
  customerName?: string;
}
