package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.PetService;
import com.petclinic.customers.datalayer.*;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * @author lpsim
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@RequestMapping("/owners/{ownerId}/pets")
@RestController
@Timed("petclinic.pet")
@Slf4j
class PetResource {

    private final PetService petService;

    public PetResource(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pet createNewPet(@RequestBody PetRequest petRequest, @PathVariable("ownerId") int ownerId)
    {
        return petService.CreatePet(petRequest, ownerId);
    }

    @GetMapping
    public List<Pet> findAll(@PathVariable("ownerId") int ownerId)
    {
        return petService.findAll(ownerId);
    }

    @GetMapping("/{petId}")
    public PetDetails findPet(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId)
    {
        return new PetDetails(findPetById(ownerId, petId).get());

    }

    private Optional<Pet> findPetById(int ownerId, int petId) 
    {
        return petService.findByPetId(ownerId, petId);
    }
  
    @DeleteMapping(value = "/{petId}")
    public void DeletePet(@PathVariable("petId") int petId, @PathVariable("ownerId") int ownerId)
    {
        petService.deletePet(petId, ownerId);
    }

}
