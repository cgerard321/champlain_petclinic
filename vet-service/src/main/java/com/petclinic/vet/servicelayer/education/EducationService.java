package com.petclinic.vet.servicelayer.education;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EducationService {
    Flux<EducationResponseDTO> getAllEducationsByVetId(String vetId);
    Mono<Void> deleteEducationByEducationId(String vetId, String educationId);


}
