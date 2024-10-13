package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Album;
import com.petclinic.vet.dataaccesslayer.AlbumRepository;
import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService{

    private final AlbumRepository albumRepository;

    @Override
    public Flux<Album> getAllAlbumsByVetId(String vetId) {
        return albumRepository.findAllByVetId(vetId)  // Fetch all albums by vetId
                .doOnSubscribe(subscription -> log.debug("Fetching all albums for vetId: {}", vetId))
                .doOnNext(album -> log.info("Album ID: {}, Vet ID: {}, Filename: {}, ImgType: {}",
                        album.getId(), album.getVetId(), album.getFilename(), album.getImgType()))
                .switchIfEmpty(Flux.error(new NotFoundException("No albums found for vet " + vetId)))
                .doOnComplete(() -> log.info("Successfully fetched all albums for vetId: {}", vetId))
                .doOnError(error -> log.error("Error fetching albums for vetId: {}", vetId, error));
    }
    @Override
    public Mono<Void> deleteAlbumPhotoById(String vetId, Integer Id) {
        return albumRepository.findById(Id)
                .switchIfEmpty(Mono.error(new NotFoundException("Album photo not found: " + Id)))
                .flatMap(albumRepository::delete);
    }

    @Override
    public Mono<Album> addPhotoToAlbum(String vetId, String filename, String imgType, byte[] data) {
        // Log the incoming parameters
        log.info("Received request to add photo to album for vetId: {}, filename: {}, imgType: {}", vetId, filename, imgType);

        // Create a new Album entity with the uploaded data
        Album albumPhoto = Album.builder()
                .vetId(vetId)
                .filename(filename)
                .imgType(imgType)
                .data(data)
                .build();

        // Log album creation
        log.info("Created Album entity for vetId: {}, filename: {}", vetId, filename);

        // Save the album to the repository
        return albumRepository.save(albumPhoto)
                .doOnSuccess(album -> log.info("Added photo {} for vet {}", filename, vetId))
                .doOnError(error -> log.error("Error adding photo for vet {}: {}", vetId, error.getMessage()));
    }


}
