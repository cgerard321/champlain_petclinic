package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetDTO;
import com.petclinic.customersservice.business.PetDTOService;
import com.petclinic.customersservice.business.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/petdto")
public class PetDTOController {

    @Autowired
    private PetDTOService petDTOService;

    @GetMapping("/{petId}")
    public Mono<PetDTO> getPetByPetId(@PathVariable String petId) {
        return petDTOService.getPetDTOByPetId(petId);
    }

}
