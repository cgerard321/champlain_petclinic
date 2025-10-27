package com.petclinic.vet.businesslayer.vets;
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

import com.petclinic.vet.presentationlayer.files.FileRequestDTO;
import com.petclinic.vet.presentationlayer.vets.VetRequestDTO;
import com.petclinic.vet.presentationlayer.vets.VetResponseDTO;
import com.petclinic.vet.presentationlayer.vets.SpecialtyDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VetService {

    Flux<VetResponseDTO> getAll();
    Mono<VetResponseDTO> addVet(Mono<VetRequestDTO> vetRequestDto);
    Mono<VetResponseDTO> updateVet(String vetId, Mono<VetRequestDTO> vetRequestDto);
    Mono<VetResponseDTO> getVetByVetId(String vetId);
    Mono<VetResponseDTO> deleteVetByVetId(String vetId);
    Flux<VetResponseDTO> getVetByIsActive(boolean isActive);
    Mono<VetResponseDTO> getVetByVetBillId(String vetBillId);
    Mono<VetResponseDTO> addSpecialtiesByVetId(String vetId, Mono<SpecialtyDTO> specialtyDTO);
    Mono<Void> deleteSpecialtiesBySpecialtyId(String vetId, String specialtyId);
    Mono<VetResponseDTO> getVetByVetId(String vetId, boolean includePhoto);
    Mono<VetResponseDTO> updateVetPhoto(String vetId, FileRequestDTO photo);
}
