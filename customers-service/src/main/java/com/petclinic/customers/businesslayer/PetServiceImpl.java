package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.*;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.presentationlayer.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PetServiceImpl implements PetService {

    private static final Logger LOG = LoggerFactory.getLogger(PetServiceImpl.class);

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    public PetServiceImpl(OwnerRepository ownerRepository, PetRepository petRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository; }


    /**
     * ------------------------ FIND ------------------------
     * This method will find one specific pet in the database and display its data
     * It is not use by the login system
     */
    @Override
    public Optional<Pet> findByPetId(int petId) {
        try {
            //Search pet in database with the given id
            Optional<Pet> pet = petRepository.findById(petId);
            if (!pet.isPresent()) {
                throw new ResourceNotFoundException("Pet "+ petId +" not found");
            }
            LOG.debug("Pet with ID: " + petId + " has been found");
            return pet;
        }
        catch (Exception e)
        {
            // Exception if pet not found
            throw new NotFoundException("Pet not found!");
        }
    }

    /**
     * ------------------------ FIND ALL ------------------------
     * This method will find all pet in the database
     */
    @Override
    public List<Pet> findAll() {

        return petRepository.findAll();
    }

    @Override
    public void updatePet() {

    }

    @Override
    public void deletePet(int Id) {

    }


    /**
     * ------------------------ CREATE ALL ------------------------
     * This method will create a new pet, assign it to an owner and save its data in repository
     */
    @Override
    public Pet CreatePet(Pet pet)
    {
        return null;
    }



}
