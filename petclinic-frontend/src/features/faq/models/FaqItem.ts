export interface FaqItem {
  id: string;

  question: string;

  answer: string;

  tags?: string[];

  category?: string;

  updatedAt?: string;
}
