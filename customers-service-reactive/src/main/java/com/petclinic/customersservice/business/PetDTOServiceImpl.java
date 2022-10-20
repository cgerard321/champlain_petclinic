package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetDTOServiceImpl implements PetDTOService {

    private PetService petService;
    private PetTypeService petTypeService;
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
}
