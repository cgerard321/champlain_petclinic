export interface PetResponseModel {
  ownerId: string;
  petId: string;
  name: string;
  birthDate: Date;
  petTypeId: string;
  isActive: string;
  weight: string;
  photo?: FileResponseModel;
}

export interface FileResponseModel {
  photoId: string;
  filename: string;
  contentType: string;
  size: number;
  data: string;
}
