package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Photo;
import reactor.core.publisher.Mono;

public interface PhotoService {

    Mono<Photo> insertPhoto(Mono<Photo> photoMono);

}
