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
    public Mono<OwnerDTO> getOwnerDTOByOwnerId(int ownerId) {
        return null;
    }
}
