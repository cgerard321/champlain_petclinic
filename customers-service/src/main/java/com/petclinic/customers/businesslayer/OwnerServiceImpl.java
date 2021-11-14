package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import com.petclinic.customers.customerExceptions.exceptions.InvalidInputException;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.datalayer.Pet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OwnerServiceImpl implements OwnerService {

    private static final Logger LOG = LoggerFactory.getLogger(OwnerServiceImpl.class);


    private final OwnerRepository repository;

    public OwnerServiceImpl(OwnerRepository repository) {
        this.repository = repository;
    }


    @Override
    public Optional<Owner> findByOwnerId(int id) {
        try {
            //Search owner in database with the given id
            Optional<Owner> owner = repository.findById(id);
            LOG.debug("Owner with ID: " + id + " has been found");
            return owner;
        } catch (Exception e) {
            // Exception if owner not found
            throw new NotFoundException("User with ID: " + id + " is not found!");
        }
    }

    @Override
    public List<Owner> findAll() {

        return repository.findAll();
    }

    @Override
    public Owner updateOwner(int id, Owner newOwner) {
        try{
            Optional<Owner> optionalOwner = repository.findById(id);
            Owner foundOwner = optionalOwner.get();
            foundOwner.setFirstName(newOwner.getFirstName());
            foundOwner.setLastName(newOwner.getLastName());
            foundOwner.setAddress(newOwner.getAddress());
            foundOwner.setCity(newOwner.getCity());
            foundOwner.setTelephone(newOwner.getTelephone());
            LOG.debug("updateOwner: owner with id {} updated",id);
           return repository.save(foundOwner);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
           throw new NotFoundException("updateOwner failed, owner with id: " + id + " not found.");
       }
    }

    @Override
    public Owner createOwner(Owner owner) {
        try{
            LOG.debug("createOwner: owner with id {} saved",owner.getId());
            return repository.save(owner);
        }
        catch(DuplicateKeyException duplicateKeyException){
            throw new InvalidInputException("Duplicate key, ownerId: " + owner.getId());
        }
    }

    @Override
    public void deleteOwner(int Id) {

        repository.findById(Id).ifPresent(o -> repository.delete(o));
        LOG.debug("User with ID: " + Id + " has been deleted successfully.");
    }

}
