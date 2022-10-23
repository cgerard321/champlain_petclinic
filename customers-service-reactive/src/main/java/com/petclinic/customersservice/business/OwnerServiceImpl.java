package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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

    @Override
    public Mono<Owner> updateOwner(String ownerId, Mono<Owner> ownerMono) {
        return repo.findById(ownerId)
                .flatMap(p -> ownerMono
                        .doOnNext(e -> e.setId(p.getId()))
                        .doOnNext(e -> e.setFirstName(p.getFirstName()))
                        .doOnNext(e -> e.setLastName(p.getLastName()))
                        .doOnNext(e -> e.setAddress(p.getAddress()))
                        .doOnNext(e -> e.setCity(p.getCity()))
                        .doOnNext(e -> e.setTelephone(p.getTelephone()))
                        .doOnNext(e -> e.setPhotoId(p.getPhotoId()))
                )
                .flatMap(repo::save);
    }

    @Override
    public Flux<Owner> getAllOwners() {
        return repo.findAll();
    }
}
