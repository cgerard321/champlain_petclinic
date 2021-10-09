package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.*;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.presentationlayer.PetRequest;
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

    private final OwnerService ownerService;

    public PetServiceImpl(PetRepository petRepository, OwnerService ownerService) {
        this.petRepository = petRepository;
        this.ownerService = ownerService;
    }


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


    /**
     * ------------------------ CREATE ALL ------------------------
     * This method will create a new pet, assign it to an owner and save its data in repository
     */
    @Override
    public Pet CreatePet(PetRequest petRequest, int ownerId)
    {
        Pet pet = new Pet();
        Optional<Owner> optionalOwner = ownerService.findByOwnerId(ownerId);
        Owner owner = optionalOwner.orElseThrow(() -> new NotFoundException("Owner "+ ownerId +" not found"));
        owner.addPet(pet);

        pet.setName(petRequest.getName());
        pet.setBirthDate(petRequest.getBirthDate());
        petRepository.findPetTypeById(petRequest.getTypeId())
                .ifPresent(pet::setType);

        LOG.info("Saving pet {}", pet);
        return petRepository.save(pet);
    }

    /**
     * ------------------------ Delete Pet ------------------------
     * This method will delete a pet
     */
    @Override
    public void deletePet(int petId, int ownerId) {

        //Search the pet
        Optional<Pet> optionalPet = petRepository.findById(petId);
        Pet pet = optionalPet.orElseThrow(()-> new NotFoundException("Pet with ID: " + petId + " has not been found"));

        //Search pet owner
        Optional<Owner> optionalOwner = ownerService.findByOwnerId(ownerId);
        Owner owner = optionalOwner.orElseThrow(() -> new NotFoundException("Owner "+ ownerId +" not found"));

        //Remove pet from owner list of pet
        owner.removePet(pet);

        //Delete pet
        petRepository.delete(pet);
        LOG.debug("Pet with ID: " + petId + " has been deleted successfully.");
    }

    /**
     * ------------------------ FIND ALL PET TYPES ------------------------
     * This method will return all pet types from the database
     */
    @Override
    public List<PetType> getAllPetTypes() {
        return petRepository.findPetTypes();
    }


}
