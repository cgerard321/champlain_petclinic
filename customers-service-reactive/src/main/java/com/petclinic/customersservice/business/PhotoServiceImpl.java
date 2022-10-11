package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.OwnerRepo;
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
    public Mono<Photo> insertPhoto(Mono<Photo> PhotoMono) {
        return PhotoMono
                .flatMap(photoRepo::insert);
    }
    @Override
    public Mono<Photo> getPhotoByPhotoId(String photoId) {
        return photoRepo.findPhotoById(photoId);
              //  .flatMap(photoRepo::findPhotoById());

    }

    @Override
    public Flux<Photo> getAll() {
        return photoRepo.findAll();
                //.flatMap(photoRepo::findAll);
    }

    @Override
    public Mono<Photo> setOwnerPhoto(Mono<Photo> PhotoMono, int ownerId) {
        return PhotoMono
                .flatMap(photoRepo::save);
    }

    @Override
    public Mono<Photo> setPetPhoto(Mono<Photo> PhotoMono, int petId) {
        return PhotoMono
                .flatMap(photoRepo::save);
    }

    @Override
    public Mono<Photo> getOwnerPhoto(int ownerId) {
        return null;
    }

    @Override
    public Mono<Photo> getPetPhoto(int petId) {
        return null;
    }

    @Override
    public Mono<Void> deletePhoto(int photoId) {
        return null;
    }
}
