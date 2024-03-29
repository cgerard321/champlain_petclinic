package com.petclinic.vet.servicelayer.education;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EducationService {
    Flux<EducationResponseDTO> getAllEducationsByVetId(String vetId);
    Mono<Void> deleteEducationByEducationId(String vetId, String educationId);
    Mono<EducationResponseDTO> updateEducationByVetIdAndEducationId(String vetId, String educationId, Mono<EducationRequestDTO> educationRequestDTOMono);


    Mono<EducationResponseDTO> addEducationToVet(String vetId, Mono<EducationRequestDTO> educationRequestDTOMono);
}
