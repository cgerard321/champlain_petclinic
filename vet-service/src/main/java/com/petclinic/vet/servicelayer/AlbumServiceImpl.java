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

}
