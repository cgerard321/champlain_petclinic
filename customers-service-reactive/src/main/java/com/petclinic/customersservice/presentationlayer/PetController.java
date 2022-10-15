package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.business.PetService;
import com.petclinic.customersservice.data.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/owners/{ownerId}/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping()
    public Flux<Pet> getAll() {
        return petService.getAll();
    }

    @PostMapping
    public Mono<Pet> insertPet(@RequestBody Mono<Pet> petMono) {
        return petService.insertPet(petMono);
    }

    @DeleteMapping("/{petId}")
    public Mono<Void> deletePet(@PathVariable("petId") int petId) {
        return petService.deletePet(petId);
    }


}
