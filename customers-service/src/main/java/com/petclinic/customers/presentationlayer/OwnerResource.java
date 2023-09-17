package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.OwnerService;
import com.petclinic.customers.businesslayer.PhotoService;
import com.petclinic.customers.datalayer.*;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * @author lpsim
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@RequestMapping("/owner")
@RestController
@Timed("petclinic.owner")
@Slf4j
class OwnerResource {
    private final OwnerService ownerService;

    private final PhotoService photoService;

    OwnerResource(OwnerService ownerService, PhotoService photoService) {
        this.ownerService = ownerService;
        this.photoService = photoService;
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Owner createOwner(@RequestBody Owner owner) {
        return ownerService.createOwner(owner);
    }

    @GetMapping(value = "/{ownerId}")
    public Optional<Owner> findOwner(@PathVariable("ownerId") int ownerId) {
        return ownerService.findByOwnerId(ownerId);
    }

    @GetMapping
    public List<Owner> findAll() {
        return ownerService.findAll();
    }

    @PutMapping(value = "/{ownerId}")
    public Owner updateOwner(@PathVariable int ownerId, @RequestBody Owner ownerRequest) {
        return ownerService.updateOwner(ownerId, ownerRequest);
    }

    @DeleteMapping(value = "/{ownerId}")
    public void deleteOwner(@PathVariable("ownerId") int ownerId) {
        ownerService.deleteOwner(ownerId);
    }

    @PostMapping(value = "/photo/{ownerId}")
    public String setPhoto(@RequestBody Photo photo, @PathVariable("ownerId") int id){
        return photoService.setOwnerPhoto(photo,id);
    }

    @GetMapping(value = "/photo/{ownerId}")
    public Photo getPhoto(@PathVariable("ownerId") int id) {
         return photoService.getOwnerPhoto(id);
    }

    @DeleteMapping(value = "/photo/{photoId}")
    public void deletePhoto(@PathVariable("photoId") int photoId) {
        photoService.deletePhoto(photoId);
    }

}


