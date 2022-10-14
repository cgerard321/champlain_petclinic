package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PhotoService;
import com.petclinic.customersservice.data.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @PostMapping
    public Mono<Photo> insertPhoto(@RequestBody Mono<Photo> photoMono) {
        return photoService.insertPhoto(photoMono);
    }

    @GetMapping("/{photoId}")
    public Mono<Photo> getPhotoByPhotoId(@PathVariable int photoId) {
        return photoService.getPhotoByPhotoId(photoId);
    }


}
