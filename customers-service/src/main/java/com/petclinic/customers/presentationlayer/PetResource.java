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
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@RequestMapping("/owners/*/pets")
@RestController
@Timed("petclinic.pet")
@Slf4j
class PetResource {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;
    private final PetService petService;

    public PetResource(PetRepository petRepository, OwnerRepository ownerRepository, PetService petService) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
        this.petService = petService;
    }

    @GetMapping("/petTypes")
    public List<PetType> getPetTypes() {

        return petRepository.findPetTypes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pet createNewPet(@RequestBody PetRequest petRequest, @PathVariable("ownerId") int ownerId) {

        Pet pet = petService.CreatePet(petRequest, ownerId);
        return pet;
    }

    @PutMapping("/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePet(@RequestBody PetRequest petRequest, @PathVariable("petId") String petId) {
        int petIdNum = petRequest.getId();
        Pet pet = findPetById(petIdNum).get();
        save(pet, petRequest);
    }

    private Pet save(final Pet pet, final PetRequest petRequest) {

        pet.setName(petRequest.getName());
        pet.setBirthDate(petRequest.getBirthDate());

        petRepository.findPetTypeById(petRequest.getTypeId())
                .ifPresent(pet::setType);

        log.info("Saving pet {}", pet);
        return petRepository.save(pet);
    }

    @GetMapping("/{petId}")
    public PetDetails findPet(@PathVariable("petId") int petId) {

        //Call external method to get pet from petService
        return new PetDetails(findPetById(petId).get());
    }


    private Optional<Pet> findPetById(int petId) {

        //Call petService to search repo using petId
        return petService.findByPetId(petId);

    }

}
