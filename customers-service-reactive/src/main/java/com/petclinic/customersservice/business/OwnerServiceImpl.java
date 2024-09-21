package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.function.Predicate;

@Slf4j
@Service
public class OwnerServiceImpl implements OwnerService {

    @Autowired
    OwnerRepo ownerRepo;

    // insertOwner has been updated, now sets a UUID for ownerId rather than leave null
    @Override
    public Mono<Owner> insertOwner(Mono<Owner> ownerMono) {
        return ownerMono.flatMap(ownerRepo::insert);
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
                        existingOwner.setProvince(requestDTO.getProvince());
                        existingOwner.setTelephone(requestDTO.getTelephone());
                        return existingOwner;
                    } ))
                    .flatMap(ownerRepo::save)
                    .map(EntityDTOUtil::toOwnerResponseDTO);
        }



    @Override
    public Flux<OwnerResponseDTO> getAllOwners() {
        return ownerRepo.findAll()
                .map(EntityDTOUtil::toOwnerResponseDTO);
    }

    @Override
    public Mono<Long> getTotalNumberOfOwnersWithFilters(String ownerId, String firstName, String lastName, String phoneNumber, String city) {
         Predicate<Owner> filterCriteria = owner ->
                (ownerId == null || owner.getOwnerId().equals(ownerId)) &&
                (firstName == null || owner.getFirstName().equals(firstName)) &&
                (lastName == null || owner.getLastName().equals(lastName)) &&
                (phoneNumber == null || owner.getTelephone().equals(phoneNumber)) &&
                (city == null || owner.getCity().equals(city));

        return ownerRepo.findAll()
                .filter(filterCriteria) // Apply filtering
                .map(EntityDTOUtil::toOwnerResponseDTO)
                .count();
    }

    @Override
    public Flux<OwnerResponseDTO> getAllOwnersPagination(Pageable pageable,
                                                         String ownerId,
                                                         String firstName,
                                                         String lastName,
                                                         String phoneNumber,
                                                         String city){

        Predicate<Owner> filterCriteria = owner ->
                (ownerId == null || owner.getOwnerId().equals(ownerId)) &&
                (firstName == null || owner.getFirstName().equals(firstName)) &&
                (lastName == null || owner.getLastName().equals(lastName)) &&
                (phoneNumber == null || owner.getTelephone().equals(phoneNumber)) &&
                (city == null || owner.getCity().equals(city));

        if(ownerId == null && firstName == null && lastName == null && phoneNumber == null && city == null){
            return ownerRepo.findAll()
                    .map(EntityDTOUtil::toOwnerResponseDTO)
                    .skip(pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize());
        } else {
            return ownerRepo.findAll()
                    .filter(filterCriteria)
                    .map(EntityDTOUtil::toOwnerResponseDTO)
                    .skip(pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize());
        }
    }



}
