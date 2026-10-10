export interface Bill {
  billId: string;
  customerId: string;
  customerFirstName: string;
  customerLastName: string;
  visitType: string;
  vetId: string;
  vetFirstName: string;
  vetLastName: string;
  date: Date;
  amount: number;
  taxedAmount: number;
  gstAmount: number;
  qstAmount: number;
  interest: number;
  totalAmount: number;
  billStatus: string;
  dueDate: Date;
  timeRemaining: number;
  interestExempt: boolean;
  archive: boolean;
}

export interface BillRequestModel {
  customerId: string;
  visitType: string;
  vetId: string;
  date: Date;
  amount: number | null;
  billStatus: string;
  dueDate: Date;
}
