package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetService;
import com.petclinic.customersservice.data.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping("/{petId}")
    public Mono<Pet> getPetDTOByPetId(@PathVariable String petId) {
        return petService.getPetById(petId);
    }

    @DeleteMapping("/{petId}")
    public Mono<Void> deletePetByPetId(@PathVariable String petId) {
        return petService.deletePetByPetId(petId);
    }

    @PostMapping
    public Mono<Pet> insertPet(@RequestBody Mono<Pet> petMono) {
        return petService.insertPet(petMono);
    }

    @PutMapping("/{petId}")
    public Mono<ResponseEntity<Pet>> updatePetByPetId(@PathVariable String petId, @RequestBody Mono<Pet> petMono) {
        return petService.updatePetByPetId(petId, petMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping()
    public Flux<Pet> getAllPets() {
        return petService.getAllPets();
    }

}
