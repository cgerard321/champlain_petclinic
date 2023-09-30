package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import com.petclinic.vet.exceptions.NotFoundException;
import com.petclinic.vet.util.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;

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
                    log.debug("Picture byte array in vet-service toServiceImpl" + resource);

                    return resource;
                });
    }
}

