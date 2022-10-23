package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.Photo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@AutoConfigureWebTestClient
class PetDTOServiceImplTest {

    @MockBean
    private PetRepo repo;

    @Autowired
    private PetDTOService petDTOService;

    Date date = new Date(20221010);

    @Test
    void GetPetDTOByPetID() throws ParseException {
        Pet petEntity = buildPet();

    }

    private Pet buildPet() {
        return Pet.builder()
                .id("55")
                .name("Test Pet")
                .petTypeId("5")
                .photoId("3")
                .birthDate(date)
                .ownerId("4")
                .build();
    }

}
