package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.customersExceptions.ApplicationExceptions;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.util.EntityDTOUtil;
import com.petclinic.customersservice.util.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/owners/petTypes")
public class PetTypeController {

    @Autowired
    PetTypeService petTypeService;

    @GetMapping()
    public Flux<PetTypeResponseDTO> getAllPetTypes() {
        return petTypeService.getAllPetTypes();
    }

    @PostMapping
    public Mono<ResponseEntity<PetTypeResponseDTO>> addPetType(@RequestBody Mono<PetTypeRequestDTO> petTypeRequestDTOMono){
        return petTypeRequestDTOMono
                .transform(Validator.validatePetType())
                .as(petTypeService::addPetType)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{petTypeId}")
    public Mono<ResponseEntity<PetTypeResponseDTO>> updatePetType(@RequestBody Mono<PetTypeRequestDTO> petTypeRequestDTO, @PathVariable String petTypeId) {
        return Mono.just(petTypeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidPetTypeId(petTypeId))
                .thenReturn(petTypeRequestDTO.transform(Validator.validatePetType()))
                .flatMap(request -> petTypeService.updatePetType(request, petTypeId))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.petTypeNotFound(petTypeId));
    }


    @GetMapping("/{petTypeId}")
    public Mono<ResponseEntity<PetTypeResponseDTO>> getPetTypeByPetTypeId(@PathVariable String petTypeId) {
        return Mono.just(petTypeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidPetTypeId(petTypeId))
                .flatMap(petTypeService::getPetTypeByPetTypeId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.petTypeNotFound(petTypeId));
    }


    @DeleteMapping("/{petTypeId}")
    public Mono<ResponseEntity<Void>> DeletePetTypeByPetTypeId(@PathVariable String petTypeId) {
        return Mono.just(petTypeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidPetTypeId(petTypeId))
                .flatMap(id -> petTypeService.deletePetTypeByPetTypeId(id).thenReturn(ResponseEntity.noContent().<Void>build()))
                .switchIfEmpty(ApplicationExceptions.petTypeNotFound(petTypeId));
    }

    @GetMapping("/pet-types-count")
    public Mono<ResponseEntity<Long>> getTotalNumberOfPetTypes(){
        return petTypeService.getAllPetTypes().count()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @GetMapping("/pet-types-pagination")
    public Flux<PetTypeResponseDTO> getAllPetTypesPagination(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(required = false) String petTypeId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description
    ){
        return petTypeService.getAllPetTypesPagination(
                PageRequest.of(page.orElse(0), size.orElse(5)), petTypeId, name, description);
    }

    @GetMapping("/pet-types-filtered-count")
    public Mono<Long> getTotalNumberOfPetTypesWithFilters(
            @RequestParam(required = false) String petTypeId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {

        return petTypeService.getTotalNumberOfPetTypesWithFilters(petTypeId, name, description);
    }
}
