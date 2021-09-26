package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetRepository;
import com.petclinic.customers.utils.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PetServiceImpl implements PetService {
    private static final Logger LOG = LoggerFactory.getLogger(PetServiceImpl.class);

    private final PetRepository repository;

    public PetServiceImpl(PetRepository repository) { this.repository = repository; }

    @Override
    public Optional<Pet> findByPetId(int Id) {
        try {
            //Search pet in database with the given id
            Optional<Pet> pet = repository.findById(Id);
            LOG.debug("Pet with ID: " + Id + " has been found");
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
    public Pet CreatePet(Pet pet) {
        return null;
    }

    @Override
    public void deletePet(int Id) {
        // TO DO
    }
}
