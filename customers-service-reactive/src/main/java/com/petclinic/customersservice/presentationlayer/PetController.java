package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetDTO;
import com.petclinic.customersservice.business.PetDTOService;
import com.petclinic.customersservice.business.PetService;
import com.petclinic.customersservice.data.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
