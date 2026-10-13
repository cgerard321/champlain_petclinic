export interface BillRecord {
  billId: string;
  customerId: string;
  customerFirstName: string | null;
  customerLastName: string | null;
  vetId: string;
  vetFirstName: string | null;
  vetLastName: string | null;
  visitType: string;
  date: Date;
  dueDate: string;
  amount: number;
  totalAmount: number | null;
  billStatus: 'PAID' | 'UNPAID' | 'OVERDUE';
  archive: boolean | null;

  taxedAmount?: number | null;
  gstAmount?: number | null;
  qstAmount?: number | null;
  interest?: number | null;
  interestExempt?: boolean;
  timeRemaining?: number | null;
}

export interface BillRequestModel {
  customerId: string;
  visitType: string;
  vetId: string;
  date: Date;
  amount: number | null;
}
