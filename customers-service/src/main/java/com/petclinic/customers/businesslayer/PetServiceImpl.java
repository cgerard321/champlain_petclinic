package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetRepository;
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

    private final PetRepository repository;
    private final OwnerRepository ownerRepository;

    public PetServiceImpl(OwnerRepository ownerRepository, PetRepository repository) {
        this.ownerRepository = ownerRepository;
        this.repository = repository; }

    @Override
    public Optional<Pet> findByPetId(int petId) {
        try {
            //Search pet in database with the given id
            Optional<Pet> pet = repository.findById(petId);
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

    @Override
    public List<Pet> findAll() {

        return repository.findAll();
    }

    @Override
    public void updatePet() {
        // TO DO
    }

    @Override
    public Pet CreatePet(PetRequest petRequest, int ownerId)
    {
        Pet pet = new Pet();
        Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
        Owner owner = optionalOwner.orElseThrow(() -> new ResourceNotFoundException("Owner "+ ownerId +" not found"));
        owner.addPet(pet);

        pet.setName(petRequest.getName());
        pet.setBirthDate(petRequest.getBirthDate());
        repository.findPetTypeById(petRequest.getTypeId())
                .ifPresent(pet::setType);

        LOG.info("Saving pet {}", pet);
        return repository.save(pet);
    }

    @Override
    public void deletePet(int Id) {
        repository.findById(Id).ifPresent(x -> repository.delete(x));
        LOG.debug("Pet with ID: " + Id + " has been deleted successfully.");
    }


}
