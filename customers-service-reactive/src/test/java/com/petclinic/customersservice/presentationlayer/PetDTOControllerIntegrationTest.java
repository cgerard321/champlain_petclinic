package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetServiceImpl;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.Photo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@SpringBootTest
@AutoConfigureWebTestClient
class PetDTOControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetServiceImpl petDTOService;

//    @Test
//    void getPetDTOByPetId() throws ParseException {
//        PetDTO petEntity = petDTObuilder();
//        String PET_ID = "1";
//        Publisher<PetDTO> setup = petDTOService.getPetDTOByPetId(Mono.just(petEntity));
//    }

    private PetResponseDTO petDTObuilder() throws ParseException {
        return PetResponseDTO.builder()
                .name("felix")
                .petTypeId("1")
                .birthDate(new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"))
                .petTypeId(PetType.builder().id(1).name("TESTPETTYPE").build().toString())
                .photoId(Photo.builder().id("1").photo("1").name("test").type("test").build().toString())
                .ownerId("ownerId-123")
                .isActive("true")
                .build();
    }

}
