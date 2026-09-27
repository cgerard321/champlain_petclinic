export interface Bill {
  billId: string;
  customerId: string;
  customerFirstName: string;
  customerLastName: string;
  visitType: string;
  vetId: string;
  vetFirstName: string;
  vetLastName: string;
  date: string;
  amount: number;
  taxedAmount: number;
  interest: number;
  billStatus: string;
  dueDate: string;
  timeRemaining: number;
  isInterestExempt: boolean;
  archive: boolean;
}
