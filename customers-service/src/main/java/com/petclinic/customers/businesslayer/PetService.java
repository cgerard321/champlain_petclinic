package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetType;
import com.petclinic.customers.presentationlayer.PetRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface PetService {
    //FIND PET
    public Optional<Pet> findByPetId(int ownerId, int petId);

    //FIND ALL PETS
    public List<Pet> findAll(int ownerId);

    //CREATE PET
    public Pet CreatePet(PetRequest pet, int ownerId);

    //DELETE PET
    public void deletePet(int petId, int ownerId);

    //GET PET TYPES
    public List<PetType> getAllPetTypes();
}
