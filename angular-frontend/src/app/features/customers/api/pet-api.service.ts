import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pet, PetType, PetRequest } from '../models/pet.model';
import { ApiConfigService } from '../../../shared/api/api-config.service';

@Injectable({
  providedIn: 'root'
})
export class PetApiService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);

  
  getPetById(petId: string): Observable<Pet> {
    return this.http.get<Pet>(this.apiConfig.getFullUrl(`/pets/${petId}`), {
      withCredentials: true
    });
  }

  
  getPetByIdFresh(petId: string): Observable<Pet> {
    return this.http.get<Pet>(this.apiConfig.getFullUrl(`/pets/${petId}?_=${new Date().getTime()}`), {
      headers: { 'Cache-Control': 'no-cache' },
      withCredentials: true
    });
  }

  
  createPet(ownerId: string, pet: PetRequest): Observable<Pet> {
    return this.http.post<Pet>(this.apiConfig.getFullUrl(`/owners/${ownerId}/pets`), pet, {
      withCredentials: true
    });
  }

  
  updatePet(petId: string, pet: PetRequest): Observable<Pet> {
    return this.http.put<Pet>(this.apiConfig.getFullUrl(`/pets/${petId}`), pet, {
      withCredentials: true
    });
  }

  
  deletePet(petId: string): Observable<void> {
    return this.http.delete<void>(this.apiConfig.getFullUrl(`/pets/${petId}`), {
      withCredentials: true
    });
  }

  
  getAllPetTypes(): Observable<PetType[]> {
    return this.http.get<PetType[]>(this.apiConfig.getFullUrl('/owners/petTypes'), {
      withCredentials: true
    });
  }

  
  createPetType(petType: Omit<PetType, 'petTypeId'>): Observable<PetType> {
    return this.http.post<PetType>(this.apiConfig.getFullUrl('/owners/petTypes'), petType, {
      withCredentials: true
    });
  }

  
  updatePetType(petTypeId: string, petType: Partial<PetType>): Observable<PetType> {
    return this.http.put<PetType>(this.apiConfig.getFullUrl(`/owners/petTypes/${petTypeId}`), petType, {
      withCredentials: true
    });
  }

  
  deletePetType(petTypeId: string): Observable<void> {
    return this.http.delete<void>(this.apiConfig.getFullUrl(`/owners/petTypes/${petTypeId}`), {
      withCredentials: true
    });
  }
}

