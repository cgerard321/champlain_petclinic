package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.data.PetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/owners/petTypes")
public class PetTypeController {

    @Autowired
    private PetTypeService petTypeService;

    @GetMapping
    public Flux<PetType> getAll() {
        return petTypeService.getAll();
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deletePetTypeByID(@PathVariable int id){
        return petTypeService.deletePetTypeByID(id);
    }

    @PutMapping("/{id}")
    public Mono<PetType> updatePetType(@PathVariable int id, @RequestBody Mono<PetType> petTypeMono){
        return petTypeService.updatePetType(id, petTypeMono);
    }

    @PostMapping()
    public Mono<PetType> insertPetType(@RequestBody Mono<PetType> petTypeMono){
        return petTypeService.insertPetType(petTypeMono);
    }
}
