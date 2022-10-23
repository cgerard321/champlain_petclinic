package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.data.Owner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    @GetMapping("/{ownerId}")
    public Mono<Owner> getOwnerByOwnerId(@PathVariable String ownerId) {
        return ownerService.getOwnerByOwnerId(ownerId);
    }

    @PostMapping()
    public Mono<Owner> insertOwner(@RequestBody Mono<Owner> ownerMono) {
        return ownerService.insertOwner(ownerMono);
    }

    @DeleteMapping("/{ownerId}")
    public Mono<Void> deleteOwnerByOwnerId(@PathVariable String ownerId) {
        return ownerService.deleteOwner(ownerId);
    }

    @PutMapping("/{ownerId}")
    public Mono<ResponseEntity<Owner>> updateOwnerByOwnerId(@PathVariable String ownerId, @RequestBody Mono<Owner> ownerMono) {
        return ownerService.updateOwner(ownerId, ownerMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
