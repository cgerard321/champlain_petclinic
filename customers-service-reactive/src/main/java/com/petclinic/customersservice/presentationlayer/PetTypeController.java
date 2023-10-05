package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetTypeService;
import com.petclinic.customersservice.data.PetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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
}
