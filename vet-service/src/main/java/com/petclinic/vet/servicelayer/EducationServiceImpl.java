package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.EducationRepository;
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final ObjectMapper objectMapper;

    public EducationServiceImpl(EducationRepository educationRepository, ObjectMapper objectMapper) {
        this.educationRepository = educationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<EducationResponseDTO> getAllEducationsByVetId(String vetId) {
        return educationRepository.findAllByVetId(vetId)
                .map(EntityDtoUtil::toDTO);
    }

}
