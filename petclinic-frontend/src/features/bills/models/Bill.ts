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
