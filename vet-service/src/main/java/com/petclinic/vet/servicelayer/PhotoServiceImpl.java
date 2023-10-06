package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import com.petclinic.vet.exceptions.NotFoundException;
import com.petclinic.vet.util.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoServiceImpl implements PhotoService {
    private final PhotoRepository photoRepository;

    @Override
    public Mono<Resource> getPhotoByVetId(String vetId) {
        return photoRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("Photo for vet " + vetId + " does not exist.")))
                .map(img -> {
                    ByteArrayResource resource = new ByteArrayResource(img.getData());

                    return resource;
                });
    }

    @Override
    public Mono<Resource> insertPhotoOfVet(String vetId, String photoName, Mono<Resource> photo) {
        return photo
                .map(p -> EntityDtoUtil.toPhotoEntity(vetId, photoName, p))
                .flatMap(photoRepository::save)
                .map(img -> {
                    // Create a Resource from the photo's InputStream
                    ByteArrayResource resource = new ByteArrayResource(img.getData());
                    log.debug("Picture byte array in vet-service toServiceImpl" + resource);

                    return resource;
                });
    }



}

