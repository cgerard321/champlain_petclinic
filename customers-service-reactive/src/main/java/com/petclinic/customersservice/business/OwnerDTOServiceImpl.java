package com.petclinic.customersservice.business;

import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/*
@Service
public class OwnerDTOServiceImpl implements OwnerDTOService {

    @Autowired
    private OwnerService ownerService;
    @Autowired
    private PetDTOService petDTOService;
    @Autowired
    private PhotoService photoService;

    @Override
    public Mono<OwnerDTO> getOwnerDTOByOwnerId(String ownerId) {
        return ownerService.getOwnerByOwnerId(ownerId)
                .map(EntityDTOUtil::toOwnerDTO)
                .flatMap(x -> photoService.getPhotoByPhotoId(x.getPhotoId())
                        .map(y -> {
                            x.setPhoto(y);
                            return x;
                        }))
                .flatMap(x -> petDTOService.getPetsByOwnerId(x.getId())
                        .collectList()
                        .map(y -> {
                            x.setPets(y);
                            return x;
                        }));
    }
}
*/