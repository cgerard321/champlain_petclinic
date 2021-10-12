package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import com.petclinic.customers.customerExceptions.exceptions.InvalidInputException;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerServiceImpl implements OwnerService {

    private static final Logger LOG = LoggerFactory.getLogger(OwnerServiceImpl.class);


    private final OwnerRepository repository;

    public OwnerServiceImpl(OwnerRepository repository){
        this.repository = repository;
    }


    /**
     * ------------------------ FIND ------------------------
     * This method will find one specific owner in the database and display its data
     * It is not use by the login system
     * @return
     */
    @Override
    public Optional<Owner> findByOwnerId(int id) {
        try {
            //Search owner in database with the given id
            Optional<Owner> owner = repository.findById(id);
            LOG.debug("Owner with ID: " + id + " has been found");
            return owner;
        }
        catch (Exception e)
        {
            // Exception if owner not found
            throw new NotFoundException("User is not found!");
        }
    }

    /**
     * ------------------------ FIND ALL ------------------------
     * This method will find one specific owner in the database and display its data
     * It is not use by the login system
     */
    @Override
    public List<Owner> findAll() {

        return repository.findAll();
    }

    /**
     * ------------------------ FIND AN ACCOUNT ------------------------
     * PRIORITY -> IMPORTANT
     * This method is used to search a user in the database based on the information he entered
     * Is used by the login system
     */


    @Override
    public void updateOwner(int id, Owner newOwner) {
       Optional<Owner> optionalOwner = repository.findById(id);
       if(optionalOwner.isPresent()){
           Owner foundOwner = optionalOwner.get();
           foundOwner.setFirstName(newOwner.getFirstName());
           foundOwner.setLastName(newOwner.getLastName());
           foundOwner.setAddress(newOwner.getAddress());
           foundOwner.setCity(newOwner.getCity());
           foundOwner.setTelephone(newOwner.getTelephone());
           LOG.debug("updateOwner: owner with id {} updated",id);
       }else{
           throw new NotFoundException("updateOwner failed, owner with id: " + id + " not found.");
       }
    }


    @Override
    public Owner createOwner(Owner owner) {
        try{
            Owner savedOwner = repository.save(owner);
            LOG.debug("createOwner: owner with id {} saved",owner.getId());
            return savedOwner;
        }catch(DuplicateKeyException duplicateKeyException){
            throw new InvalidInputException("Duplicate key, ownerId: " + owner.getId());
        }
        //INSERT METHOD
    }



    /**
     * ------------------------ DELETE ------------------------
     * This method will find one specific owner in the database and delete its data
     */
    @Override
    public void deleteOwner(int Id) {

        repository.findById(Id).ifPresent(o -> repository.delete(o));
        LOG.debug("User with ID: " + Id + " has been deleted successfully.");



    }
}
