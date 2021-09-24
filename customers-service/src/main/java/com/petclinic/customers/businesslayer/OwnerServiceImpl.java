package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import com.petclinic.customers.utils.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OwnerServiceImpl implements OwnerService {

    private static final Logger LOG = LoggerFactory.getLogger(OwnerServiceImpl.class);

    private OwnerRepository repository;

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
    public Optional<Owner> findByOwnerId(int Id) {
        try {

            //Search owner in database with the given id
            Optional<Owner> owner = repository.findById(Id);
            LOG.debug("Owner with ID: " + Id + " has been found");
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
    /*
    @Override
    public Optional<Owner> findOwnerLogin(String username, String password) {
        try {
            Optional<Owner> account = repository.findAccount(username, password);
            LOG.debug("User has been found. Logging in now.");
            return account;
        }
        catch (Exception e)
        {
            throw new NotFoundException("Error has occurred. The information entered is incorrect. Please try again");
        }
    }
    */


    @Override
    public void updateOwner() {

        //INSERT METHOD
    }


    @Override
    public Owner CreateOwner(Owner owner) {
        return null;
        //INSERT METHOD
    }



    /**
     * ------------------------ DELETE ------------------------
     * This method will find one specific owner in the database and delete its data
     */
    @Override
    public void deleteOwner(int Id) {

    /*
        if (repository.findById(Id).isPresent())
        {
            repository.deleteById(Id);
        }
        else
        {
            throw new NotFoundException("Tried to delete ID: " + Id + " but was not found");
        }


        if (repository.findById(Id).isPresent())
        {
            Optional<Owner> owner = repository.findById(Id);
            repository.delete(owner.get());
            LOG.debug("User with ID: " + Id + " has been deleted successfully.");
        }
        else
        {
            LOG.debug("ERROR WITH ID: " + Id);
        }


    */
        repository.findById(Id).ifPresent(o -> repository.delete(o));
        LOG.debug("User with ID: " + Id + " has been deleted successfully.");



    }
}
