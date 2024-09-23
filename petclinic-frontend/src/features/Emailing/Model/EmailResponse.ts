export interface EmailModelResponseDTO {
  id: number;
  email: string;
  subject: string;
  body: string; // HTML body
  emailStatus: string;
}