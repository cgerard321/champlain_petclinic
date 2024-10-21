export interface ReceivedEmailModel {
  from: string;
  subject: string;
  dateReceived: Date;
  plainTextBody: string;
}
