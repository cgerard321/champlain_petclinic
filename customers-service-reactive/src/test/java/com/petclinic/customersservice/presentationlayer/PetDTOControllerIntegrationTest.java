package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetDTO;
import com.petclinic.customersservice.business.PetDTOServiceImpl;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.Photo;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static reactor.core.publisher.Mono.just;

@SpringBootTest
@AutoConfigureWebTestClient
class PetDTOControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetDTOServiceImpl petDTOService;

//    @Test
//    void getPetDTOByPetId() throws ParseException {
//        PetDTO petEntity = petDTObuilder();
//        String PET_ID = "1";
//        Publisher<PetDTO> setup = petDTOService.getPetDTOByPetId(Mono.just(petEntity));
//    }

    private PetDTO petDTObuilder() throws ParseException {
        return PetDTO.builder()
                .id("1")
                .name("felix")
                .petTypeId("1")
                .birthDate(new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"))
                .petType(PetType.builder().id("1").name("TESTPETTYPE").build())
                .photo(Photo.builder().id("1").photo("1").name("test").type("test").build())
                .ownerId("1")
                .build();
    }

}
