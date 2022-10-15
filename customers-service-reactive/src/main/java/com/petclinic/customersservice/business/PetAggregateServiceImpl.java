package com.petclinic.customersservice.business;

import com.petclinic.customersservice.util.EntityAggregateUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetAggregateServiceImpl implements PetAggregateService {

    private PetServiceImpl petService;
    private PetTypeServiceImpl petTypeService;
    private PhotoServiceImpl photoService;

    @Override
    public Mono<PetAggregate> getPetAggregateByPetId(int petId) {
        return petService.getPetByPetId(petId)
                .map(EntityAggregateUtil::toPetAggregate)
                .flatMap(x -> photoService.getPhotoByPhotoId(x.getPhoto()))
                .flatMap(y -> petTypeService.getPetTypeByPetTypeId(y.getType());
    }
}
