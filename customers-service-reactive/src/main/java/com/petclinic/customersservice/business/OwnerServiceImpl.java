package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OwnerServiceImpl implements OwnerService {

    @Autowired
    OwnerRepo ownerRepo;

    // insertOwner has been updated, now sets a UUID for ownerId rather than leave null
    @Override
    public Mono<Owner> insertOwner(Mono<Owner> ownerMono) {
        return ownerMono.map(owner -> {
            owner.setOwnerId(UUID.randomUUID().toString());
            return owner;
        }).flatMap(ownerRepo::insert);
    }


    // get owner by ownerId has been updated and now return
    @Override
    public Mono<OwnerResponseDTO> getOwnerByOwnerId(String ownerId) {
        return ownerRepo.findOwnerByOwnerId(ownerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Owner not found with id : " + ownerId)))
                .map(EntityDTOUtil::toOwnerResponseDTO);
    }

    @Override
    public Mono<Void> deleteOwner(String ownerId) {
        return ownerRepo.deleteById(ownerId);
    }

    @Override
    public Mono<Owner> updateOwner(String ownerId, Mono<Owner> ownerMono) {
        return ownerRepo.findById(ownerId)
                /*.flatMap(p -> ownerMono
                        .doOnNext(e -> e.setId(p.getId()))
                        .doOnNext(e -> e.setPhotoId(p.getPhotoId()))
                )*/
                .flatMap(ownerRepo::save);
    }

    @Override
    public Flux<OwnerResponseDTO> getAllOwners() {
        return ownerRepo.findAll()
                .map(EntityDTOUtil::toOwnerResponseDTO);
    }

}
