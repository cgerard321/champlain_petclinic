package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.util.EntityAggregateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.petclinic.customersservice.business.OwnerServiceImpl;


@Service
public class OwnerAggregateServiceImpl implements OwnerAggregateService {

    private OwnerServiceImpl ownerService;
    //private PetAggregate;
    private PhotoServiceImpl photoService;
    private PetTypeServiceImpl petTypeService;

    @Override
    public Mono<OwnerAggregate> getOwnerAggregateByOwnerId(int ownerId) {
        return ownerService.getOwnerByOwnerId(ownerId)
                .map(EntityAggregateUtil::toOwnerAggregate)
                .flatMap(x -> photoService.getPhotoByPhotoId(x.getPhoto().getId()))
                .flatMap(x -> petService.);
    }
}
