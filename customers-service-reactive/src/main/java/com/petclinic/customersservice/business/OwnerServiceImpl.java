package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OwnerServiceImpl implements OwnerService {

    @Autowired
    OwnerRepo repo;

    @Override
    public Mono<Owner> insertOwner(Mono<Owner> ownerMono) {
        return ownerMono
                .flatMap(repo::insert);
    }

    @Override
    public Mono<Owner> getOwnerByOwnerId(String Id) {
        return repo.findOwnerById(Id);
    }

    @Override
    public Mono<Void> deleteOwner(String ownerId) {
        return repo.deleteById(ownerId);
    }
}
