package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.data.PetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/owners/petTypes")
public class PetTypeController {

    @Autowired
    private PetTypeService petTypeService;

    @GetMapping
    public Flux<PetType> getAll() {
        return petTypeService.getAll();
    }


}
