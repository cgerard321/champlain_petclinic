/*
package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.dataaccesslayer.VetRepository;
import com.petclinic.vet.presentationlayer.VetRequestDTO;
import com.petclinic.vet.presentationlayer.VetResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureWebTestClient
class VetServiceImplTest {

    @Autowired
    VetService vetService;
    @Autowired
    PhotoService photoService;

    @MockBean
    VetRepository vetRepository;
    //To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    VetResponseDTO vetResponseDTO = buildVetResponseDTO();
    VetRequestDTO vetRequestDTO = buildVetRequestDTO();
    Vet vet = buildVet();
    String VET_ID = vet.getVetId();
    String VET_BILL_ID = vet.getVetBillId();

    @Test
    void getAllVets() {
        when(vetRepository.findAll()).thenReturn(Flux.just(vet));

        Flux<VetResponseDTO> vetDTO = vetService.getAll();

        StepVerifier
                .create(vetDTO)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getVetId(), foundVet.getVetId());
                    assertEquals(vet.getVetBillId(), foundVet.getVetBillId());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                    assertFalse(foundVet.isActive());
                })
                .verifyComplete();
    }

    @Test
    void createVet() {
        vetService.insertVet(Mono.just(vetRequestDTO))
                .map(vetDTO1 -> {
                    assertEquals(vetDTO1.getVetId(), vetRequestDTO.getVetId());
                    assertEquals(vetDTO1.getEmail(), vetRequestDTO.getEmail());
                    assertEquals(vetDTO1.getResume(), vetRequestDTO.getResume());
                    assertEquals(vetDTO1.getLastName(), vetRequestDTO.getLastName());
                    assertEquals(vetDTO1.getFirstName(), vetRequestDTO.getFirstName());
                    assertEquals(vetDTO1.getWorkday(), vetRequestDTO.getWorkday());
                    assertEquals(vetDTO1.getPhoneNumber(), vetRequestDTO.getPhoneNumber());
                    assertEquals(vetDTO1.getSpecialties(), vetRequestDTO.getSpecialties());
                    return vetDTO1;
                });
    }


    @Test
    void updateVet() {
        when(vetRepository.save(any(Vet.class))).thenReturn(Mono.just(vet));

        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(vet));
        vetService.updateVet(VET_ID, (Mono.just(vetRequestDTO)))
                .map(vetDTO1 -> {
                    assertEquals(vetDTO1.getVetId(), vetRequestDTO.getVetId());
                    assertEquals(vetDTO1.getEmail(), vetRequestDTO.getEmail());
                    assertEquals(vetDTO1.getResume(), vetRequestDTO.getResume());
                    assertEquals(vetDTO1.getLastName(), vetRequestDTO.getLastName());
                    assertEquals(vetDTO1.getFirstName(), vetRequestDTO.getFirstName());
                    assertEquals(vetDTO1.getWorkday(), vetRequestDTO.getWorkday());
                    assertEquals(vetDTO1.getPhoneNumber(), vetRequestDTO.getPhoneNumber());
                    assertEquals(vetDTO1.getSpecialties(), vetRequestDTO.getSpecialties());
                    return vetDTO1;
                });
    }

    @Test
    void getVetByVetId() {

        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(vet));

        Mono<VetResponseDTO> vetDTOMono = vetService.getVetByVetId(VET_ID);


        StepVerifier
                .create(vetDTOMono)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getVetId(), foundVet.getVetId());
                    assertEquals(vet.getVetBillId(), foundVet.getVetBillId());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                    assertFalse(foundVet.isActive());
                })

                .verifyComplete();
    }

    @Test
    void getVetByVetBillId() {

        when(vetRepository.findVetByVetBillId(anyString())).thenReturn(Mono.just(vet));

        Mono<VetResponseDTO> vetDTOMono = vetService.getVetByVetBillId(VET_BILL_ID);


        StepVerifier
                .create(vetDTOMono)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getVetId(), foundVet.getVetId());
                    assertEquals(vet.getVetBillId(), foundVet.getVetBillId());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                    assertFalse(foundVet.isActive());
                })

                .verifyComplete();
    }

    @Test
    void getVetByIsInactive() {

        when(vetRepository.findVetsByActive(anyBoolean())).thenReturn(Flux.just(vet));

        Flux<VetResponseDTO> vetDTO = vetService.getVetByIsActive(vet.isActive());

        StepVerifier
                .create(vetDTO)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getVetId(), foundVet.getVetId());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                    assertFalse(foundVet.isActive());
                })

                .verifyComplete();
    }

    @Test
    void getVetByIsActive() {
        Vet vet = buildVet2();

        when(vetRepository.findVetsByActive(anyBoolean())).thenReturn(Flux.just(vet));

        Flux<VetResponseDTO> vetDTO = vetService.getVetByIsActive(vet.isActive());

        StepVerifier
                .create(vetDTO)
                .consumeNextWith(foundVet -> {
                    assertEquals(vet.getVetId(), foundVet.getVetId());
                    assertEquals(vet.getFirstName(), foundVet.getFirstName());
                    assertEquals(vet.getLastName(), foundVet.getLastName());
                    assertEquals(vet.getEmail(), foundVet.getEmail());
                    assertEquals(vet.getPhoneNumber(), foundVet.getPhoneNumber());
                    assertEquals(vet.getResume(), foundVet.getResume());
                    assertEquals(vet.getWorkday(), foundVet.getWorkday());
                    assertTrue(foundVet.isActive());
                })

                .verifyComplete();
    }


    @Test
    void deleteVet() {
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(vetRepository.delete(any())).thenReturn(Mono.empty());

        Mono<Void> deletedVet=vetService.deleteVetByVetId(VET_ID);

        StepVerifier
                .create(deletedVet)
                .verifyComplete();
    }


    private Vet buildVet() {
        return Vet.builder()
                .vetId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .vetBillId("1")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
<<<<<<< HEAD
                .workday(new HashSet<>())
=======
                .workday("Monday")
>>>>>>> 8d8e3440 (Tests modified in vet-service)
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private VetRequestDTO buildVetRequestDTO() {
        return VetRequestDTO.builder()
                .vetId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .vetBillId("53c2d16e-1ba3-4dbc-8e31-6decd2eaa99a")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private VetResponseDTO buildVetResponseDTO() {
        return VetResponseDTO.builder()
                .vetId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .vetBillId("53c2d16e-1ba3-4dbc-8e31-6decd2eaa99a")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
<<<<<<< HEAD
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private VetDTO buildVetDTO() {
        return VetDTO.builder()
                .vetId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .vetBillId("53c2d16e-1ba3-4dbc-8e31-6decd2eaa99a")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
=======
                .workday("Monday")
>>>>>>> 8d8e3440 (Tests modified in vet-service)
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private Vet buildVet2() {
        return Vet.builder()
                .vetId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .vetBillId("74cb3b00-2808-499c-9bf6-15e94d9eacc7")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
<<<<<<< HEAD
                .workday(new HashSet<>())
=======
                .workday("Monday")
>>>>>>> 8d8e3440 (Tests modified in vet-service)
                .specialties(new HashSet<>())
                .active(true)
                .build();
    }
}
*/
