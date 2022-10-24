package com.petclinic.customersservice.business;

import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PetDTOServiceImpl implements PetDTOService {

    @Autowired
    private PetService petService;

    @Autowired
    private PetTypeService petTypeService;

    @Autowired
    private PhotoService photoService;

    @Override
    public Mono<PetDTO> getPetDTOByPetId(String petId) {
        return petService.getPetById(petId)
                .map(EntityDTOUtil::toPetDTO)
                .flatMap(x -> petTypeService.getPetTypeById(x.getPetTypeId())
                    .map(y -> {
                        x.setPetType(y);
                        return x;
                    })
                )
                .flatMap(x -> photoService.getPhotoByPhotoId(x.getPhotoId())
                    .map(y -> {
                        x.setPhoto(y);
                        return x;
                    })
                );
    }

    @Override
    public Flux<PetDTO> getPetsByOwnerId(String ownerId) {
        return petService.getPetsByOwnerId(ownerId)
                .map(EntityDTOUtil::toPetDTO)
                .flatMap(x -> petTypeService.getPetTypeById(x.getPetTypeId())
                        .map(y -> {
                            x.setPetType(y);
                            return x;
                        })
                )
                .flatMap(x -> photoService.getPhotoByPhotoId(x.getPhotoId())
                        .map(y -> {
                            x.setPhoto(y);
                            return x;
                        })
                );
    }

}
