export interface Photo {
  vetId: string;
  photo: string;
  filename: string;
  resourceBase64?: string;
}

export interface PhotoRequest {
  name: string;
  type: string;
  photo: string;
}
