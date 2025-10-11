package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureWebTestClient
class PetDTOServiceImplTest {

    @MockBean
    private PetRepo repo;

    @Autowired
    private PetService petDTOService;

    Date date = new Date(20221010);

//    @Test
//    void getPetDTOByPetId() throws ParseException {
//        PetDTO petDTO = petDTObuilder();
//        String PET_ID = petDTO.getPetTypeId();
//
//        Mono<PetDTO> petDTOMono = petDTOService.getPetDTOByPetId(PET_ID);
//
//
//    }

//    @Test
//    void GetPetDTOByPetID() throws ParseException {
//        Pet petEntity = buildPet();
//
//        String PET_ID = petEntity.getId();
//
//        when(repo.findPetById(anyString())).thenReturn(Mono.just(petEntity));
//
//        Mono<PetDTO> petDTOMono = petDTOService.getPetDTOByPetId(PET_ID);
//
//        StepVerifier.create(petDTOMono)
//                .consumeNextWith(foundPet ->{
//                    assertEquals(petEntity.getId(), foundPet.getId());
//                    assertEquals(petEntity.getName(), foundPet.getName());
//                    assertEquals(petEntity.getPetTypeId(), foundPet.getPetTypeId());
//                    assertEquals(petEntity.getPhotoId(), foundPet.getPhotoId());
//                    assertEquals(petEntity.getBirthDate(), foundPet.getBirthDate());
//                    assertEquals(petEntity.getOwnerId(), foundPet.getOwnerId());
//                })
//                .verifyComplete();
//
//    }

    private Pet buildPet() {
        return Pet.builder()
                .id("55")
                .name("Test Pet")
                .petTypeId("5")
                .photoId("3")
                .birthDate(date)
                .ownerId("ownerId-123")
                .isActive("true")
                .build();
    }

    private PetResponseDTO petDTObuilder() throws ParseException {
        return PetResponseDTO.builder()
                .name("felix")
                .petTypeId("1")
                .birthDate(new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"))
                .petTypeId(PetType.builder().id("1").name("TESTPETTYPE").build().toString())
                //.photoId(Photo.builder().id("1").photo("1").name("test").type("test").build().toString())
                .ownerId("ownerId-1234")
                .isActive("true")
                .build();
    }

}
