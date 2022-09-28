package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.OwnerService;
import com.petclinic.customers.businesslayer.PetService;
import com.petclinic.customers.datalayer.Owner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
class OwnerAPITest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OwnerService ownerService;

    @MockBean
    PetService petService;


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
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Wick"))
                .andExpect(jsonPath("$.address").value("56 John St."))
                .andExpect(jsonPath("$.city").value("Amsterdam"))
                .andExpect(jsonPath("$.telephone").value("9999999999"));
    }


    /**
     * ------------------------ DELETE_OWNER_API_TEST ------------------------
     * Test an HTTP Delete Request
     */
    @Test
    void deleteOwner_API_TEST() throws Exception {
        mvc.perform(delete("/owners/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(ownerService, times(1)).deleteOwner(5);
    }

    /**
     * ------------------------ SETUP_OWNER ------------------------
     * Test an HTTP Get Request, but to get all owners
     */
    @Test
    void findAll_API_TEST() throws Exception {

        //TEST DATA
        Owner owner_1 = new Owner();
        owner_1.setId(1);
        owner_1.setFirstName("John");
        owner_1.setLastName("Wick");
        owner_1.setAddress("56 John St.");
        owner_1.setCity("Amsterdam");
        owner_1.setTelephone("9999999999");

        Owner owner_2 = new Owner();
        owner_2.setId(2);
        owner_2.setFirstName("Sean");
        owner_2.setLastName("Bean");
        owner_2.setAddress("678 Rue Tremblay");
        owner_2.setCity("Montreal");
        owner_2.setTelephone("0123456789");

        Owner owner_3 = new Owner();
        owner_3.setId(3);
        owner_3.setFirstName("Jean-Michel");
        owner_3.setLastName("Test");
        owner_3.setAddress("111 Test St.");
        owner_3.setCity("Testopolis");
        owner_3.setTelephone("9876543210");

        given(ownerService.findAll()).willReturn(asList(owner_1, owner_2, owner_3));
        mvc.perform(get("/owners").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3));

    }

    /**
     * ------------------------ CREATE_OWNER ------------------------
     * Test an HTTP POST request
     */
    @Test
    void createOwner_API_TEST() throws Exception {
        Owner owner = setupOwner();
        when(ownerService.createOwner(any(Owner.class))).thenReturn(owner);
        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"John\"," + "\"lastName\": \"Wick\"," + "\"address\": \"56 John St.\"," + "\"city\": \"Amsterdam\"," + "\"telephone\": \"9999999999\"}"))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

}



