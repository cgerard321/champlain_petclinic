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

    @Autowired
    PhotoRepo repo;

    @Override
    public Mono<Photo> insertPhoto(Mono<Photo> photoMono) {
        return photoMono
                .flatMap(repo::insert);
    }
}
