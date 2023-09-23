package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
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
    public Mono<OwnerResponseDTO> updateOwner(Mono<OwnerRequestDTO> ownerRequestDTO, String ownerId) {

        return ownerRepo.findOwnerByOwnerId(ownerId)
                .flatMap(existingOwner -> ownerRequestDTO.map(requestDTO -> {
                    existingOwner.setFirstName(requestDTO.getFirstName());
                    existingOwner.setLastName(requestDTO.getLastName());
                    existingOwner.setAddress(requestDTO.getAddress());
                    existingOwner.setCity(requestDTO.getCity());
                    existingOwner.setTelephone(requestDTO.getTelephone());
                    return existingOwner;
                }))
                .flatMap(ownerRepo::save)
                .map(EntityDTOUtil::toOwnerResponseDTO);
    }


    @Override
    public Flux<OwnerResponseDTO> getAllOwners() {
        return ownerRepo.findAll()
                .map(EntityDTOUtil::toOwnerResponseDTO);
    }

}


