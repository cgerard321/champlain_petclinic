package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.util.EntityDTOUtil;
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
                .doOnNext(event -> {
                    if (event.getOwnerId() == null) {
                        event.setOwnerId(EntityDTOUtil.generateUUIDString());
                    }
                })
                .flatMap(repo::insert);
    }

    @Override
    public Mono<Owner> getOwnerByOwnerId(String Id) {
        return repo.findOwnerByOwnerId(Id);
    }

    @Override
    public Mono<Void> deleteOwner(String ownerId) {
        return repo.deleteByOwnerId(ownerId);
    }

    @Override
    public Mono<OwnerDTO> updateOwner(String ownerId, Mono<OwnerDTO> ownerDTOMono) {
        return repo.findById(ownerId)
                .flatMap(p -> ownerDTOMono
                        .map(EntityDTOUtil::toOwner)
                        .doOnNext(e -> e.setId(p.getId()))
                        .doOnNext(e -> e.setOwnerId(p.getOwnerId()))
                        .doOnNext(e -> e.setPhotoId(p.getPhotoId()))
                )
                .flatMap(repo::save)
                .map(EntityDTOUtil::toOwnerDTO);
    }

    @Override
    public Flux<OwnerDTO> getAllOwners() {
        return repo.findAll().map(EntityDTOUtil::toOwnerDTO);
    }

}
