package com.petclinic.customersservice.presentationlayer;


import com.petclinic.customersservice.business.OwnerDTO;
import com.petclinic.customersservice.business.OwnerDTOService;
import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.data.Owner;
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

    @Autowired
    private OwnerService ownerService;

//    @GetMapping("/{ownerId}")
//    public Mono<OwnerDTO> getOwnerDTOByOwnerId(@PathVariable String ownerId) {
//        return ownerDTOService.getOwnerDTOByOwnerId(ownerId);
//    }
    @GetMapping("/{ownderId}")
    public Mono<Owner> getOwnerByOwnerId(@PathVariable String ownerId) {
        return ownerService.getOwnerByOwnerId(ownerId);
    }

}
