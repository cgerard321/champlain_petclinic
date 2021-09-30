package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@ExtendWith(SpringExtension.class)
//@WebMvcTest(OwnerResource.class)
@DataJpaTest
// @Transactional(propagation = NOT_SUPPORTED)
class OwnerResourceTest {

    /*
    @Autowired
    MockMvc mvc;
    */

    @Autowired
    OwnerRepository repository;
    private Owner ownerBean;

    /**
     * ------------------------ DELETE_REPO ------------------------
     * Simple void method to delete the repo when testing
     */
    @BeforeEach
    public void setUpDB()
    {

        repository.deleteAll();
        Owner o = new Owner(1,"Obama","Barack","102 rue Hitchinton","Tallahassee","9999999999");
        ownerBean = repository.save(o);

        // assertThat(ownerBean, samePropertyValuesAs(o));
    }

    /**
     * ------------------------ SETUP_OWNER ------------------------
     * Simple method to create an owner
     */
    private Owner setupOwner(Integer id, String firstname, String lastname, String address, String city, String telephone)
    {
        Owner owner = new Owner(id, firstname, lastname, address, city, telephone);
        /*
        owner.setFirstName(firstname);
        owner.setLastName(lastname);
        owner.setAddress(address);
        owner.setCity(city);
        owner.setTelephone(telephone);
        */
        return owner;
    }

    /**
     * ------------------------ TEST_FIND ------------------------
     * Testing the find by id method
     * ___NOT WORKING
     */
    @Test
    public void findById()
    {
        Owner newOwner = new Owner();
        newOwner.setFirstName("John");
        newOwner.setLastName("Smith");
        newOwner.setTelephone("5551234");
        newOwner.setCity("Montreal");
        newOwner.setAddress("123Main");

        Owner savedOwner = repository.save(newOwner);

        Owner foundOwner = repository.findById(savedOwner.getId()).orElse(null);

        assert foundOwner != null;
    }

    /**
     * ------------------------ TEST_FIND_ALL ------------------------
     * Testing the find_all() method
     * ___NOT WORKING
     */
    @Test
    public void findAll()
    {
        int expectedLength = 4;
        Owner owner1 = new Owner (2, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal",
                "1111111111");
        repository.save(owner1);
        Owner owner2 = new Owner (3, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal",
                "1111111111");
        repository.save(owner2);
        Owner owner3 = new Owner (4, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal",
                "1111111111");
        repository.save(owner3);
        Owner owner4 = new Owner (5, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal",
                "1111111111");
        repository.save(owner4);

        /*
        if (repository.findAll().size() == expectedLength)
        {
            System.out.println("Yay");
        }
        else
        {
            System.out.println("No");
        }

         */
        assertEquals(expectedLength, repository.findAll().size());

        // repository.deleteAll();
    }



    /**
     * ------------------------ TEST_DELETE ------------------------
     * Testing the delete owner method
     * ___NOT WORKING
     */
    @Test
    public void deleteOwner()
    {
        /*

        int OwnerID = 1;
        //Owner owner = setupOwner("Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "(111) 111-1111");
        Owner owner = new Owner (OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "(111) 111-1111");
        repository.save(owner);


        repository.findById(OwnerID).ifPresent(o -> repository.delete(o));
        assertEquals(expectedRes, repository.findAll().size());

         */

        int OwnerID = 1;
        int expectedRes = 0;

        Owner newOwner = new Owner (OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        repository.save(newOwner);

        if (repository.findById(OwnerID).isPresent())
        {
            Optional<Owner> ownerOptional = repository.findById(OwnerID);
            Owner owner = ownerOptional.get();
            repository.delete(owner);


            if (repository.findById(OwnerID).isPresent() == false)
            {
                System.out.println("Yay!");
            }
        }
        else
        {
            System.out.println("No");
        }

    }
    /*
    @Test
    public void add_owner_test()
    {
        // final int OWNER_ID = 1;
        Owner owner1 = new Owner (2, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal",
                "1111111111");
        repository.save(owner1);;
        // repository.save(owner);
        Owner foundSaved = repository.findById(owner1.getId()).get();

        assertThat(foundSaved, samePropertyValuesAs(owner1));

        assertEquals(2,repository.findAll().size());
    }


*/
    // Reset the database
    @AfterEach
    public void resetDb()
    {
        repository.deleteAll();
    }
}

