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
  dueDate: Date;
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

export type BillUpdateRequest = Pick<
  BillRecord,
  'customerId' | 'vetId' | 'visitType' | 'amount' | 'billStatus'
> & {
  date: string;
  dueDate: string;
};
