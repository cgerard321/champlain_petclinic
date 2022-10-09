package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PhotoServiceImpl implements PhotoService {

    @Override
    public Mono<Photo> setOwnerPhoto(Mono<Photo> PhotoMono, int ownerId) {
        return null;
    }

    @Override
    public Mono<Photo> setPetPhoto(Mono<Photo> PhotoMono, int petId) {
        return null;
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
