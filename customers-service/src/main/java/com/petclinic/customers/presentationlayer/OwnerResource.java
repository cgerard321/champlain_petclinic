package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.OwnerService;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.Photo;
import com.petclinic.customers.datalayer.PhotoRepository;
import com.petclinic.customers.datalayer.ResponseMessage;
import com.petclinic.customers.util.PhotoUtil;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.json.Json;

import javax.print.attribute.standard.Media;
import javax.validation.Valid;
import java.io.IOException;
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

@RequestMapping("/owners")
@RestController
@Timed("petclinic.owner")
@Slf4j
class OwnerResource {
    private final OwnerService ownerService;

    @Autowired
    PhotoRepository photoRepository;

    OwnerResource(OwnerService ownerService) {
        this.ownerService = ownerService;
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



//    @PostMapping("/upload/photo")
//    public ResponseEntity<ResponseMessage> uploadImage(@RequestParam("image") MultipartFile file)
//            throws IOException {
//
//        photoRepository.save(Photo.builder()
//                .name(file.getOriginalFilename())
//                .type(file.getContentType())
//                .photo(PhotoUtil.compressImage(file.getBytes())).build());
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(new ResponseMessage("Image uploaded successfully: " +
//                        file.getOriginalFilename()));
//    }

    @PostMapping(value = "/upload/photo")
    public String setPhoto(@RequestBody Photo photo){
        photoRepository.save(photo);
        return "Image uploaded successfully: " + photo.getName();
    }

    @GetMapping(value = "/photo/{photoId}")
    public Photo getPhoto(@PathVariable("photoId") int id) {
         return photoRepository.findPhotoById(id);
    }

}


