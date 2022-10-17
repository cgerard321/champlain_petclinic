package com.petclinic.customersservice.business;

import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PetDTOServiceImpl implements PetDTOService {

    private PetServiceImpl petService;
    private PetTypeServiceImpl petTypeService;
    private PhotoServiceImpl photoService;

    @Override
    public Mono<PetDTO> getPetDTOByPetId(int petId) {
        return petService.getPetByPetId(petId)
                .map(EntityDTOUtil::toPetDTO)
                .flatMap(x -> petTypeService.getPetTypeByPetTypeId(x.))
    }
}
