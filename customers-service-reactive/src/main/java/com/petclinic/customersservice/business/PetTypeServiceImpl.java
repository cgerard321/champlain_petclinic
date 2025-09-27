package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.presentationlayer.PetTypeRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetTypeResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.function.Predicate;

@Slf4j
@Service
public class PetTypeServiceImpl implements PetTypeService {

    @Autowired
    PetTypeRepo petTypeRepo;

    @Override
    public Mono<PetType> insertPetType(Mono<PetType> petTypeMono) {
        return petTypeMono
                .flatMap(petTypeRepo::insert);
    }

    @Override
    public Flux<PetTypeResponseDTO> getAllPetTypes() {
        return petTypeRepo.findAll()
                .map(EntityDTOUtil::toPetTypeResponseDTO);
    }

    @Override
    public Mono<PetTypeResponseDTO> getPetTypeByPetTypeId(String petTypeId) {

        return petTypeRepo.findOPetTypeById(petTypeId)
                .switchIfEmpty(Mono.error(new NotFoundException("Pet Type not found with id : " + petTypeId)))
                .map(EntityDTOUtil::toPetTypeResponseDTO);

    }

    @Override
    public Mono<PetTypeResponseDTO> updatePetType(Mono<PetTypeRequestDTO> petTypeRequestDTO, String petTypeId) {
        return petTypeRepo.findOPetTypeById(petTypeId)
                .flatMap(existingPetType -> petTypeRequestDTO.map(requestDTO -> {
                    existingPetType.setName(requestDTO.getName());
                    existingPetType.setPetTypeDescription(requestDTO.getPetTypeDescription());
                    return existingPetType;
                } ))
                .flatMap(petTypeRepo::save)
                .map(EntityDTOUtil::toPetTypeResponseDTO);
    }

    @Override
    public Mono<Void> deletePetTypeByPetTypeId(String petTypeId) {
        return petTypeRepo.deleteByPetTypeId(petTypeId);
    }


    /*
    @Override
    Mono<Void> deletePetTypeByPetTypeId(String petTypeId){

            return petTypeRepo.deleteById(petTypeId);

    }




    @Override
    public Mono<PetType> getPetTypeById(Integer Id) {
        return petTypeRepo.findPetTypeById(Id);
    }*/




    @Override
    public Mono<Long> getTotalNumberOfPetTypesWithFilters(String petTypeId, String name, String description) {
        Predicate<PetType> filterCriteria = petType ->
                (petTypeId == null || petType.getPetTypeId().equals(petTypeId)) &&
                        (name == null || petType.getName().toLowerCase().contains(name.toLowerCase())) &&
                        (description == null || petType.getPetTypeDescription().toLowerCase().contains(description.toLowerCase()));

        return petTypeRepo.findAll()
                .filter(filterCriteria) // Apply filtering
                .map(EntityDTOUtil::toPetTypeResponseDTO)
                .count();
    }

    @Override
    public Flux<PetTypeResponseDTO> getAllPetTypesPagination(Pageable pageable,
                                                             String petTypeId,
                                                             String name,
                                                             String description){

        Predicate<PetType> filterCriteria = petType ->
                (petTypeId == null || petType.getPetTypeId().equals(petTypeId)) &&
                        (name == null || petType.getName().toLowerCase().contains(name.toLowerCase())) &&
                        (description == null || petType.getPetTypeDescription().toLowerCase().contains(description.toLowerCase()));

        if(petTypeId == null && name == null && description == null){
            return petTypeRepo.findAll()
                    .map(EntityDTOUtil::toPetTypeResponseDTO)
                    .skip(pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize());
        } else {
            return petTypeRepo.findAll()
                    .filter(filterCriteria)
                    .map(EntityDTOUtil::toPetTypeResponseDTO)
                    .skip(pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize());
        }
    }


}
