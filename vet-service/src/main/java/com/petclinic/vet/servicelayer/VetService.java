package com.petclinic.vet.servicelayer;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import com.petclinic.vet.dataaccesslayer.Specialty;
import com.petclinic.vet.presentationlayer.VetRequestDTO;
import com.petclinic.vet.presentationlayer.VetResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface VetService {

    Flux<VetResponseDTO> getAll();
    Mono<VetResponseDTO> addVet(Mono<VetRequestDTO> vetRequestDto);
    Mono<VetResponseDTO> updateVet(String vetId, Mono<VetRequestDTO> vetRequestDto);
    Mono<VetResponseDTO> getVetByVetId(String vetId);
    Mono<Void> deleteVetByVetId(String vetId);
    Flux<VetResponseDTO> getVetByIsActive(boolean isActive);
    Mono<VetResponseDTO> getVetByVetBillId(String vetBillId);
    Mono<VetResponseDTO> addSpecialtiesByVetId(String vetId, Mono<SpecialtyDTO> specialtyDTO);
    Mono<Void> deleteSpecialtiesBySpecialtyId(String vetId, String specialtyId);
}
