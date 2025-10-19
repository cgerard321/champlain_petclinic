export interface Bill {
  billId: string;
  customerId: string;
  vetId: string;
  visitId?: string;
  visitType: string;
  date: string;
  amount: number;
  billStatus: BillStatus;
  dueDate: string;
  description?: string;
  taxedAmount?: number;
  owner?: string;
  vet?: string;
  id?: string;
}

export enum BillStatus {
  UNPAID = 'UNPAID',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE'
}

export interface BillRequest {
  customerId: string;
  vetId: string;
  visitType: string;
  date: string;
  amount: number;
  billStatus: BillStatus;
  dueDate: string;
  description?: string;
}

export interface PaginatedBills {
  bills: Bill[];
  currentPage: number;
  totalPages: number;
  totalItems: number;
}

