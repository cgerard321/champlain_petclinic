package com.petclinic.vet.servicelayer.education;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.VetRepository;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.exceptions.NotFoundException;
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class EducationServiceImpl implements EducationService {
    private final VetRepository vetRepository;
    private final EducationRepository educationRepository;
    private final ObjectMapper objectMapper;

    public EducationServiceImpl(EducationRepository educationRepository, ObjectMapper objectMapper, VetRepository vetRepository) {
        this.educationRepository = educationRepository;
        this.objectMapper = objectMapper;
        this.vetRepository = vetRepository;

    }

    @Override
    public Flux<EducationResponseDTO> getAllEducationsByVetId(String vetId) {
        return educationRepository.findAllByVetId(vetId)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<Void> deleteEducationByEducationId(String vetId, String educationId) {
        return educationRepository.findByVetIdAndEducationId(vetId, educationId)
                .switchIfEmpty(Mono.error(new Exception("Education with id " + educationId + " not found.")))
                .flatMap(educationRepository::delete);
    }

    @Override
    public Mono<EducationResponseDTO> addEducationToVet(String vetId, Mono<EducationRequestDTO> educationRequestDTOMono) {
        return educationRequestDTOMono
                .map(EntityDtoUtil::toEntity)
                .doOnNext(r -> r.setEducationId(UUID.randomUUID().toString()))
                .flatMap(educationRepository::insert)
                .map(EntityDtoUtil::toDTO);
    }
        @Override
        public Mono<EducationResponseDTO> updateEducationByVetIdAndEducationId (String vetId, String educationId, Mono<EducationRequestDTO> educationRequestDTOMono){
            return vetRepository.findVetByVetId(vetId)
                    .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                    .then(educationRepository.findByVetIdAndEducationId(vetId, educationId)
                            .switchIfEmpty(Mono.error(new NotFoundException("educationId not found: " + educationId)))
                            .flatMap(education -> educationRequestDTOMono
                                    .map(EntityDtoUtil::toEntity)
                                    .doOnNext(e -> e.setId(education.getId())) //check
                                    .doOnNext(e -> e.setEducationId(education.getEducationId()))
                                    .flatMap(educationRepository::save)
                                    .map(EntityDtoUtil::toDTO))
                    );
        }
    }



