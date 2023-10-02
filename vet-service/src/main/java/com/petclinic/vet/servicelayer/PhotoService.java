package com.petclinic.vet.servicelayer;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

public interface PhotoService {
    Mono<Resource> getPhotoByVetId(String vetId);
}
