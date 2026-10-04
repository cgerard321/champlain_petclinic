export interface Bill {
  billId: string;
  customerId: string;
  ownerFirstName: string;
  ownerLastName: string;
  visitType: string;
  vetId: string;
  vetFirstName: string;
  vetLastName: string;
  date: string;
  amount: number;
  taxedAmount: number;
  gstAmount: number;
  qstAmount: number;
  interest: number;
  totalAmount: number;
  billStatus: string;
  dueDate: string;
  timeRemaining: number;
  interestExempt: boolean;
  archive: boolean;
}
