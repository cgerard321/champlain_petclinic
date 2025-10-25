package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import com.petclinic.customersservice.presentationlayer.PetTypeRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetTypeResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Predicate;

@Slf4j
@Service
public class PetTypeServiceImpl implements PetTypeService {

    @Autowired
    private PetTypeRepo petTypeRepo;

    @Override
    public Flux<PetTypeResponseDTO> getAllPetTypes() {
        return petTypeRepo.findAll()
                .map(EntityDTOUtil::toPetTypeResponseDTO);
    }

    @Override
    public Mono<PetTypeResponseDTO> getPetTypeByPetTypeId(String petTypeId) {
        return petTypeRepo.findByPetTypeId(petTypeId)
                .map(EntityDTOUtil::toPetTypeResponseDTO);
    }

    @Override
    public Mono<PetTypeResponseDTO> addPetType(Mono<PetTypeRequestDTO> petTypeRequestDTOMono) {
        return petTypeRequestDTOMono
                .map(EntityDTOUtil::toPetType)
                .flatMap(petTypeRepo::save)
                .map(EntityDTOUtil::toPetTypeResponseDTO);
    }

    @Override
    public Mono<PetTypeResponseDTO> updatePetType(Mono<PetTypeRequestDTO> petTypeRequestDTO, String petTypeId) {
        return petTypeRepo.findByPetTypeId(petTypeId)
                .flatMap(existingPetType -> petTypeRequestDTO
                        .map(EntityDTOUtil::toPetType)
                        .doOnNext( p -> {
                            p.setId(existingPetType.getId());
                            p.setPetTypeId(petTypeId);
                        })
                )
                .flatMap(petTypeRepo::save)
                .map(EntityDTOUtil::toPetTypeResponseDTO);
    }

    @Override
    public Mono<Void> deletePetTypeByPetTypeId(String petTypeId) {
        return petTypeRepo.deleteByPetTypeId(petTypeId);
    }

    @Override
    public Mono<Long> getTotalNumberOfPetTypesWithFilters(String petTypeId,
                                                          String name,
                                                          String description) {
        Predicate<PetType> filterCriteria = petType ->
                (petTypeId == null || petType.getPetTypeId().equals(petTypeId)) &&
                        (name == null || petType.getName().toLowerCase().contains(name.toLowerCase())) &&
                        (description == null || petType.getPetTypeDescription().toLowerCase().contains(description.toLowerCase()));

        return petTypeRepo.findAll()
                .filter(filterCriteria)
                .map(EntityDTOUtil::toPetTypeResponseDTO)
                .count();
    }

    @Override
    public Flux<PetTypeResponseDTO> getAllPetTypesPagination(Pageable pageable,
                                                             String petTypeId,
                                                             String name,
                                                             String description) {
        Predicate<PetType> filterCriteria = petType ->
                (petTypeId == null || petType.getPetTypeId().equals(petTypeId)) &&
                        (name == null || petType.getName().toLowerCase().contains(name.toLowerCase())) &&
                        (description == null || petType.getPetTypeDescription().toLowerCase().contains(description.toLowerCase()));

        Flux<PetType> source = petTypeRepo.findAll();
        if (petTypeId != null || name != null || description != null) {
            source = source.filter(filterCriteria);
        }

        return source
                .map(EntityDTOUtil::toPetTypeResponseDTO)
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .take(pageable.getPageSize());
    }
}
