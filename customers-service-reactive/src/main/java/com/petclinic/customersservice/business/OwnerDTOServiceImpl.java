package com.petclinic.customersservice.business;

import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class OwnerDTOServiceImpl implements OwnerDTOService {

    private OwnerServiceImpl ownerService;
    private PetServiceImpl petService;
    private PhotoServiceImpl photoService;
    private PetTypeServiceImpl petTypeService;

    @Override
    public Mono<OwnerDTO> getOwnerAggregateByOwnerId(int ownerId) {
        return ownerService.getOwnerByOwnerId(ownerId)
                .map(EntityDTOUtil::toOwnerDTO)
                .flatMap(x -> photoService.getPhotoByPhotoId(x.getPhoto().getId())
                .flatMap(y -> petService.getPetByPetId(y.get)));
    }
}
