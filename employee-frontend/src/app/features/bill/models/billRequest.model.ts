export interface BillRequestModel {
  customerId: string;
  ownerFirstName: string;
  ownerLastName: string;
  visitType: string;
  vetId: string;
  vetFirstName: string;
  vetLastName: string;
  date: string;
  amount: number;
  billStatus: string;
}
