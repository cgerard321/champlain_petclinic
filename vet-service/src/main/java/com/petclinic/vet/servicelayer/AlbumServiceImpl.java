package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.AlbumRepository;
import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.springframework.core.io.Resource;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService{

    private final AlbumRepository albumRepository;

    @Override
    public Flux<Resource> getAllPhotosByVetId(String vetId) {
        return albumRepository.findAllByVetId(vetId)  // Fetch all albums by vetId
                .doOnSubscribe(subscription -> log.debug("Fetching all photos for vetId: {}", vetId))
                .switchIfEmpty(Flux.error(new NotFoundException("No photos found for vet " + vetId)))
                // Flatten the list of photos from each album
                .flatMap(album -> Flux.fromIterable(album.getPhotos()))
                // Convert each Photo to Resource
                .map(this::createResourceFromPhoto)
                .cast(Resource.class)  // Explicit cast to Resource
                .doOnComplete(() -> log.info("Successfully fetched all photos for vetId: {}", vetId))
                .doOnError(error -> log.error("Error fetching photos for vetId: {}", vetId, error));
    }

    private ByteArrayResource createResourceFromPhoto(Photo photo) {
        return new ByteArrayResource(photo.getData());
    }

}
