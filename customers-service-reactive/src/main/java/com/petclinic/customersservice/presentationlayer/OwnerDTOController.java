package com.petclinic.customersservice.presentationlayer;


import com.petclinic.customersservice.business.OwnerDTO;
import com.petclinic.customersservice.business.OwnerDTOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ownerdto")
public class OwnerDTOController {

    @Autowired
    private OwnerDTOService ownerDTOService;

    @GetMapping("/{ownerId}")
    public Mono<OwnerDTO> getOwnerDTOByOwnerId(@PathVariable String ownerId) {
        return ownerDTOService.getOwnerDTOByOwnerId(ownerId);
    }

}
