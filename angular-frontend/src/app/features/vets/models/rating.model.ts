export type PredefinedDescription = 'POOR' | 'AVERAGE' | 'GOOD' | 'EXCELLENT';

export interface Rating {
  ratingId: string;
  rateScore: number;
  rateDescription: string;
  predefinedDescription?: string;
  rateDate: string;
  customerName?: string;
}

export interface RatingRequest {
  vetId: string;
  rateScore: number;
  rateDescription: string | null;
  rateDate?: string;
  predefinedDescription?: PredefinedDescription | null;
  customerName?: string;
}

export const PREDEFINED_DESCRIPTIONS: PredefinedDescription[] = [
  'POOR',
  'AVERAGE', 
  'GOOD',
  'EXCELLENT'
];
