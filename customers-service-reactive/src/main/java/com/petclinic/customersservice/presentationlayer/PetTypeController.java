package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.util.EntityDTOUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @PutMapping("/{petTypeId}")
    public Mono<ResponseEntity<PetTypeResponseDTO>> updatePetType(
            @RequestBody Mono<PetTypeRequestDTO> petTypeRequestDTO,
            @PathVariable String petTypeId) {

        return petTypeService.updatePetType(petTypeRequestDTO, petTypeId)
                .map(updatedPetType -> ResponseEntity.ok().body(updatedPetType))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @GetMapping("/{petTypeId}")
    public Mono<ResponseEntity<PetTypeResponseDTO>> getPetTypeByPetTypeId(@PathVariable String petTypeId) {
        return petTypeService.getPetTypeByPetTypeId(petTypeId)
                .map(petTypeResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(petTypeResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{petTypeId}")
    public Mono<ResponseEntity<Object>> DeletePetTypeByPetTypeId(@PathVariable String petTypeId) {
        return petTypeService.deletePetTypeByPetTypeId(petTypeId)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }




}
