import { PetRequestModel } from "@/features/customers/models/PetRequestModel";
import axiosInstance from "@/shared/api/axiosInstance";


export const getOwnerPets = async (ownerId: string): Promise<{ data: PetRequestModel[] }> => {
  const response = await axiosInstance.get(`http://localhost:8080/api/v2/gateway/owners/${ownerId}/pets`, { withCredentials: true });
  return response;
};

// Function to update the pets for an owner
export const updateOwnerPets = async (ownerId: string, pets: PetRequestModel[]): Promise<void> => {
  // Update the URL to match the backend endpoint
  await axiosInstance.put(`http://localhost:8080/api/v2/gateway/pet/owner/${ownerId}/pets`, pets, {withCredentials: true});
};