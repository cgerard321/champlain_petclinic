package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.presentationlayer.PetRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface PetService {
    //FIND PET
    public Optional<Pet> findByPetId(int petId);

    //FIND ALL PETS
    public List<Pet> findAll();

    //UPDATE PET - TO BE IMPLEMENTED
    public void updatePet();

    //CREATE PET
    public Pet CreatePet(PetRequest pet, int ownerId);

    //DELETE PET - TO BE IMPLEMENTED
    public void deletePet(int petId);
}
