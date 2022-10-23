package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PhotoServiceImpl implements PhotoService {

    @Autowired
    private PhotoRepo photoRepo;

    @Override
    public Mono<Photo> insertPhoto(Mono<Photo> photoMono) {
        return photoMono
                .flatMap(photoRepo::insert);
    }
    @Override
    public Mono<Photo> getPhotoByPhotoId(String photoId) {
        return photoRepo.findPhotoById(photoId);
    }

    @Override
    public Mono<Void> deletePhoto(String photoId) {
        return null;
    }
}
