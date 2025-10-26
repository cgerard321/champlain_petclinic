package com.petclinic.vet.servicelayer;

import com.petclinic.vet.businesslayer.photos.PhotoService;
import com.petclinic.vet.businesslayer.vets.VetService;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.vets.Specialty;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.domainclientlayer.FilesServiceClient;
import com.petclinic.vet.presentationlayer.vets.SpecialtyDTO;
import com.petclinic.vet.presentationlayer.vets.VetRequestDTO;
import com.petclinic.vet.presentationlayer.vets.VetResponseDTO;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class VetServiceImplTest {

    @Autowired
    VetService vetService;
    @Autowired
    PhotoService photoService;

    @MockBean
    VetRepository vetRepository;
    @MockBean
    FilesServiceClient filesServiceClient;
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
                    assertEquals(vet.getWorkHoursJson(), foundVet.getWorkHoursJson());
                    assertFalse(foundVet.isActive());
                })
                .verifyComplete();
    }

    @Test
    void createVet() {
        vetService.addVet(Mono.just(vetRequestDTO))
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
                    assertEquals(vet.getWorkHoursJson(), foundVet.getWorkHoursJson());
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
                    assertEquals(vet.getWorkHoursJson(), foundVet.getWorkHoursJson());
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
                    assertEquals(vet.getWorkHoursJson(), foundVet.getWorkHoursJson());
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
                .workday(new HashSet<>())
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
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
                .workday(new HashSet<>())
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
                .workday(new HashSet<>())
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
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
                .workday(new HashSet<>())
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
                .specialties(new HashSet<>())
                .active(true)
                .build();
    }

    @Test
    void deleteSpecialtyBySpecialtyId_Success() {
        String vetId = "test-vet-id";
        String specialtyId = "test-specialty-id";
        
        Specialty specialty = Specialty.builder()
                .specialtyId(specialtyId)
                .name("radiology")
                .build();
        
        Set<Specialty> specialties = new HashSet<>();
        specialties.add(specialty);
        
        Vet vet = Vet.builder()
                .vetId(vetId)
                .vetBillId("test-bill-id")
                .firstName("Test")
                .lastName("Vet")
                .email("test@vet.com")
                .phoneNumber("123-456-7890")
                .specialties(specialties)
                .build();
        
        when(vetRepository.findVetByVetId(vetId)).thenReturn(Mono.just(vet));
        when(vetRepository.save(any(Vet.class))).thenReturn(Mono.just(vet));
        
        StepVerifier.create(vetService.deleteSpecialtiesBySpecialtyId(vetId, specialtyId))
                .verifyComplete();
        
        verify(vetRepository, times(1)).findVetByVetId(vetId);
        verify(vetRepository, times(1)).save(any(Vet.class));
    }

    @Test
    void deleteSpecialtyBySpecialtyId_VetNotFound() {
        String vetId = "non-existent-vet-id";
        String specialtyId = "test-specialty-id";
        
        when(vetRepository.findVetByVetId(vetId)).thenReturn(Mono.empty());
        
        StepVerifier.create(vetService.deleteSpecialtiesBySpecialtyId(vetId, specialtyId))
                .expectErrorMatches(throwable -> 
                    throwable instanceof NotFoundException &&
                    throwable.getMessage().contains("No vet found with vetId: " + vetId))
                .verify();
        
        verify(vetRepository, times(1)).findVetByVetId(vetId);
        verify(vetRepository, never()).save(any(Vet.class));
    }

    @Test
    void deleteSpecialtyBySpecialtyId_SpecialtyNotFound() {
        String vetId = "test-vet-id";
        String specialtyId = "non-existent-specialty-id";
        
        Specialty specialty = Specialty.builder()
                .specialtyId("different-specialty-id")
                .name("radiology")
                .build();
        
        Set<Specialty> specialties = new HashSet<>();
        specialties.add(specialty);
        
        Vet vet = Vet.builder()
                .vetId(vetId)
                .vetBillId("test-bill-id")
                .firstName("Test")
                .lastName("Vet")
                .email("test@vet.com")
                .phoneNumber("123-456-7890")
                .specialties(specialties)
                .build();
        
        when(vetRepository.findVetByVetId(vetId)).thenReturn(Mono.just(vet));
        
        StepVerifier.create(vetService.deleteSpecialtiesBySpecialtyId(vetId, specialtyId))
                .expectErrorMatches(throwable -> 
                    throwable instanceof NotFoundException &&
                    throwable.getMessage().contains("No specialty found with specialtyId: " + specialtyId))
                .verify();
        
        verify(vetRepository, times(1)).findVetByVetId(vetId);
        verify(vetRepository, never()).save(any(Vet.class));
    }

    @Test
    void addSpecialtiesByVetId_Success() {
        String vetId = "test-vet-id";
        
        SpecialtyDTO specialtyDTO = SpecialtyDTO.builder()
                .specialtyId("")
                .name("surgery")
                .build();
        
        Vet vet = Vet.builder()
                .vetId(vetId)
                .vetBillId("test-bill-id")
                .firstName("Test")
                .lastName("Vet")
                .email("test@vet.com")
                .phoneNumber("123-456-7890")
                .specialties(new HashSet<>())
                .build();
        
        when(vetRepository.findVetByVetId(vetId)).thenReturn(Mono.just(vet));
        when(vetRepository.save(any(Vet.class))).thenReturn(Mono.just(vet));
        
        StepVerifier.create(vetService.addSpecialtiesByVetId(vetId, Mono.just(specialtyDTO)))
                .expectNextMatches(response -> 
                    response.getVetId().equals(vetId) &&
                    response.getSpecialties().size() == 1)
                .verifyComplete();
        
        verify(vetRepository, times(1)).findVetByVetId(vetId);
        verify(vetRepository, times(1)).save(any(Vet.class));
    }

    @Test
    void addSpecialtiesByVetId_VetNotFound() {
        String vetId = "non-existent-vet-id";
        
        SpecialtyDTO specialtyDTO = SpecialtyDTO.builder()
                .specialtyId("")
                .name("surgery")
                .build();
        
        when(vetRepository.findVetByVetId(vetId)).thenReturn(Mono.empty());
        
        StepVerifier.create(vetService.addSpecialtiesByVetId(vetId, Mono.just(specialtyDTO)))
                .expectErrorMatches(throwable -> 
                    throwable instanceof NotFoundException &&
                    throwable.getMessage().contains("Vet not found with id: " + vetId))
                .verify();
        
        verify(vetRepository, times(1)).findVetByVetId(vetId);
        verify(vetRepository, never()).save(any(Vet.class));
    }

}
