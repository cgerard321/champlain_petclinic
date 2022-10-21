package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.data.Owner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/owners")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    @GetMapping()
    public Flux<Owner> getAll(){
        return ownerService.getAll();
    }

    @PostMapping()
    public Mono<Owner> insertOwner(@RequestBody Mono<Owner> ownerMono) {
        return ownerService.insertOwner(ownerMono);
    }

    @DeleteMapping("/{ownerId}")
    public Mono<Void> deleteOwner(@PathVariable("ownerId") int ownerId) {
        return ownerService.deleteOwner(ownerId);
    }

}
