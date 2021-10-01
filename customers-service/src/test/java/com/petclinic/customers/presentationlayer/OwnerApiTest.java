package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.OwnerService;
import com.petclinic.customers.datalayer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerAPITest {

    @Autowired
    MockMvc mvc;

    @Autowired
    OwnerRepository repository;

    @Autowired
    OwnerService ownerService;


    //SIMPLE OWNER OBJECT CREATION METHOD
    //*** Used for testing only ***
    private Owner setupOwner() {

        Owner owner = new Owner();
        owner.setId(5);
        owner.setFirstName("John");
        owner.setLastName("Wick");
        owner.setAddress("56 John St.");
        owner.setCity("Amsterdam");
        owner.setTelephone("9999999999");

        return owner;
    }

    //DELETE REPO BEFORE EACH TEST
    @BeforeEach
    public void setUpDB()
    {
        repository.deleteAll();
    }


    /**
     * ------------------------ FIND_BY_ID_OWNER_API_TEST ------------------------
     * Test an HTTP Get Request
     */


    @Test
    void findByOwnerId_API_TEST() throws Exception {

        Owner owner = setupOwner();
        given(ownerService.findByOwnerId(5)).willReturn(Optional.of(owner));


        mvc.perform(get("/owners/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.first.name").value("John"))
                .andExpect(jsonPath("$.last.name").value("Wick"))
                .andExpect(jsonPath("$.address").value("56 John St."))
                .andExpect(jsonPath("$.city").value("Amsterdam"))
                .andExpect(jsonPath("$.telephone").value("9999999999"));
    }

    /**
     * ------------------------ DELETE_OWNER_API_TEST ------------------------
     * Test an HTTP Delete Request
     */
    /*
    @Test
    void deleteOwner_API_TEST() throws Exception {

        Owner owner = setupOwner();
        given(repository.findById(5)).willReturn(Optional.of(owner));


        mvc.perform(delete("/owners/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    /**
     * ------------------------ SETUP_OWNER ------------------------
     * Simple method to create an owner
     */

    /**
     * ------------------------ TEST_FIND ------------------------
     * Testing the find by id method
     * ___NOT WORKING
     */
/*
    @Test
    public void findById()
    {
        /*
        Owner owner = new Owner();
        owner.setId(1);
        when(ownerRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        Owner expected = detailUserService.listUser(owner.getId());
        assertThat(expected).isSameAs(owner);
        verify(ownerRepository).findById(owner.getId());
         */
/*
        int OwnerID = 1;

        Owner newOwner = new Owner (OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        repository.save(newOwner);

        assertTrue(repository.findById(OwnerID).isPresent());

       /*

       Owner owner = repository.findByOwnerId(newOwner.getId()).get();
        assertEquals(1, repository.count());



            int OwnerID = 1;
            Owner owner = new Owner (OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
            repository.save(owner);


        if (repository.findById(OwnerID).isPresent())
        {
            System.out.println("Yeetus happy");
        }
        else
        {
            System.out.println("Yeetus sad");
        }



        //assertThat(owner.getId()).isEqualTo(OwnerID);


    }

    /**
     * ------------------------ TEST_FIND_ALL ------------------------
     * Testing the find_all() method
     * ___NOT WORKING
     */
/*
    @Test
    public void findAll()
    {
        int expectedLength = 4;
        Owner owner1 = new Owner (1, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "(111) 111-1111");
        repository.save(owner1);
        Owner owner2 = new Owner (2, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "(111) 111-1111");
        repository.save(owner2);
        Owner owner3 = new Owner (3, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "(111) 111-1111");
        repository.save(owner3);
        Owner owner4 = new Owner (4, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "(111) 111-1111");
        repository.save(owner4);

        assertEquals(expectedLength, repository.findAll().size());
    }



    /**
     * ------------------------ TEST_DELETE ------------------------
     * Testing the delete owner method
     * ___NOT WORKING
     */
/*
    @Test
    public void deleteOwner()
    {
        int expectedRes = 0;
        int OwnerID = 1;
        //Owner owner = setupOwner("Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "(111) 111-1111");
        Owner owner = new Owner (OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "(111) 111-1111");
        repository.save(owner);
        assertEquals(expectedRes, repository.findAll().size());


        repository.findById(OwnerID).ifPresent(o -> repository.delete(o));
        assertEquals(expectedRes, repository.findAll().size());
    }
    */
}



