package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.servicelayer.PhotoService;
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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


//    @GetMapping("/{photoName}")
//    public Mono<Photo> getPhotoByPhotoName (@PathVariable String photoName){
//        return photoService.getPhotoByPhotoName(photoName);
//    }

//    @DeleteMapping(value = "/photo/{photoId}")
//    public Mono<Photo> deletePhoto(@PathVariable String photoId) {
//        return photoService.deletePhoto(photoId);
//    }
}