package com.petclinic.vet.servicelayer;


import com.petclinic.vet.dataaccesslayer.EducationRepository;
import reactor.core.publisher.Flux;

public interface EducationService {
    Flux<EducationResponseDTO> getAllEducationsByVetId(String vetId);

}
