export interface EmailNotificationModel {
  emailToSendTo: string;
  emailTitle: string;
  templateName: string;
  header: string;
  body: string;
  footer: string;
  correspondantName: string;
  senderName: string;
  sentDate: string;
}
