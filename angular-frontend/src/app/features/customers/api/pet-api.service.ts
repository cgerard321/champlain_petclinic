import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pet, PetType, PetRequest } from '../models/pet.model';
import { environment } from '../../../../environments/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class PetApiService {
  private readonly BASE_URL = environment.apiUrl;
  private http = inject(HttpClient);

  
  getPetById(petId: string): Observable<Pet> {
    return this.http.get<Pet>(`${this.BASE_URL}/pets/${petId}`, {
      withCredentials: true
    });
  }

  
  getPetByIdFresh(petId: string): Observable<Pet> {
    return this.http.get<Pet>(`${this.BASE_URL}/pets/${petId}?_=${new Date().getTime()}`, {
      headers: { 'Cache-Control': 'no-cache' },
      withCredentials: true
    });
  }

  
  createPet(ownerId: string, pet: PetRequest): Observable<Pet> {
    return this.http.post<Pet>(`${this.BASE_URL}/owners/${ownerId}/pets`, pet, {
      withCredentials: true
    });
  }

  
  updatePet(petId: string, pet: PetRequest): Observable<Pet> {
    return this.http.put<Pet>(`${this.BASE_URL}/pets/${petId}`, pet, {
      withCredentials: true
    });
  }

  
  deletePet(petId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/pets/${petId}`, {
      withCredentials: true
    });
  }

  
  getAllPetTypes(): Observable<PetType[]> {
    return this.http.get<PetType[]>(`${this.BASE_URL}/owners/petTypes`, {
      withCredentials: true
    });
  }

  
  createPetType(petType: Omit<PetType, 'petTypeId'>): Observable<PetType> {
    return this.http.post<PetType>(`${this.BASE_URL}/owners/petTypes`, petType, {
      withCredentials: true
    });
  }

  
  updatePetType(petTypeId: string, petType: Partial<PetType>): Observable<PetType> {
    return this.http.put<PetType>(`${this.BASE_URL}/owners/petTypes/${petTypeId}`, petType, {
      withCredentials: true
    });
  }

  
  deletePetType(petTypeId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/owners/petTypes/${petTypeId}`, {
      withCredentials: true
    });
  }
}

