package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class OwnerPersistenceTest {


    @Autowired
    OwnerRepository repository;


    /**
     * ------------------------ DELETE_REPO ------------------------
     * Simple void method to delete the repo when testing
     * Called before each test
     */
    @BeforeEach
    public void setUpDB()
    {
        repository.deleteAll();
    }

    /**
     * ------------------------ TEST_FIND ------------------------
     * Testing the find by id method
     */
    @Test
    public void findById()
    {
        //Set ID
        int OwnerID = 789;

        //Create new owner
        Owner newOwner = new Owner (OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        repository.save(newOwner);

        //If owner is found
        if (repository.findById(OwnerID).isPresent())
        {
            System.out.println("Owner is found!");
        }
        else
        {
            System.out.println("No");
        }




    }

    /**
     * ------------------------ TEST_FIND_ALL ------------------------
     * Testing the find_all() method
     */
    @Test
    public void findAll()
    {
        //Creates 4 new owner object
        int expectedLength = 4;
        Owner owner1 = new Owner (1, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        repository.save(owner1);
        Owner owner2 = new Owner (2, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        repository.save(owner2);
        Owner owner3 = new Owner (3, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        repository.save(owner3);
        Owner owner4 = new Owner (4, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        repository.save(owner4);



        //Make sure that 4 owners has been inserted in repo
        assertEquals(expectedLength, repository.findAll().size());
    }



    /**
     * ------------------------ TEST_DELETE ------------------------
     * Testing the delete owner method
     */
    @Test
    public void deleteOwner()
    {


        //Set ID
        int OwnerID = 1;

        //Create new owner
        Owner newOwner = new Owner (OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        repository.save(newOwner);


        //Is the owner exist?
        if (repository.findById(OwnerID).isPresent())
        {
            //if yes, delete owner
            Optional<Owner> ownerOptional = repository.findById(OwnerID);
            Owner owner = ownerOptional.get();
            repository.delete(owner);

            //Check if owner has been deleted
            if (repository.findById(OwnerID).isPresent() == false)
            {
                System.out.println("Owner has been successfully deleted!");
            }
        }
        else
        {
            //Error
            System.out.println("No");
        }

    }
}

