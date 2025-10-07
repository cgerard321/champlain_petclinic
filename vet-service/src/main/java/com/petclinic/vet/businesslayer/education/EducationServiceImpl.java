package com.petclinic.vet.businesslayer.education;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.presentationlayer.education.EducationRequestDTO;
import com.petclinic.vet.presentationlayer.education.EducationResponseDTO;
import com.petclinic.vet.utils.EntityDtoUtil;
import com.petclinic.vet.utils.exceptions.InvalidInputException;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
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
                .switchIfEmpty(Mono.error(new NotFoundException("Education with id " + educationId + " not found for vetId " + vetId)))
                .flatMap(education -> {
                    log.info("Deleting education with id {} for vetId {}", educationId, vetId);
                    return educationRepository.delete(education);
                });
    }

    @Override
    public Mono<EducationResponseDTO> addEducationToVet(String vetId, Mono<EducationRequestDTO> educationRequestDTOMono) {
        return validateVetExists(vetId)
                .then(educationRequestDTOMono.map(this::mapToEntityWithId))
                .flatMap(education -> saveEducationForVet(vetId, education))
                .map(EntityDtoUtil::toDTO)
                .onErrorResume(this::handleAddEducationError);
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

    private Mono<Void> validateVetExists(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("Vet with id " + vetId + " not found.")))
                .then();
    }

    private Education mapToEntityWithId(EducationRequestDTO educationRequestDTO) {
        Education education = EntityDtoUtil.toEntity(educationRequestDTO);
        education.setEducationId(UUID.randomUUID().toString());
        return education;
    }

    private Mono<Education> saveEducationForVet(String vetId, Education education) {
        education.setVetId(vetId);
        return educationRepository.insert(education);
    }

    private Mono<EducationResponseDTO> handleAddEducationError(Throwable error) {
        if (error instanceof InvalidInputException) {
            return Mono.error(new InvalidInputException("Invalid input data for education."));
        } else if (error instanceof NotFoundException) {
            return Mono.error(error);
        }
        return Mono.error(new Exception("Unexpected error while adding education: " + error.getMessage()));
    }
    }



