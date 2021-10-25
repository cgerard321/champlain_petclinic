package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.PetService;
import com.petclinic.customers.datalayer.PetType;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/owners/petTypes")
@RestController
@Timed("petclinic.pet")
@Slf4j
public class PetTypesResource {

    private final PetService petService;

    public PetTypesResource(PetService petService) {
        this.petService = petService;
    }

    /**
     * Get all pet types
     */
    @GetMapping
    public List<PetType> getPetTypes()
    {
        return petService.getAllPetTypes();
    }
}
