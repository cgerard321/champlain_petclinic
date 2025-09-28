package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Album;
import com.petclinic.vet.dataaccesslayer.AlbumRepository;
import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
    public Mono<Album> insertAlbumPhoto(String vetId, String photoName, MultipartFile file) {
        return Mono.fromCallable(() -> {
                    if (file == null || file.getSize() <= 0) {
                        throw new InvalidInputException("Empty file");
                    }
                    String ct = file.getContentType();
                    if (ct == null || !ct.startsWith("image/")) {
                        throw new InvalidInputException("Unsupported media type");
                    }
                    return Album.builder()
                            .vetId(vetId)
                            .filename(photoName)
                            .imgType(ct)
                            .data(file.getBytes())
                            .build();
                })
                .flatMap(albumRepository::save)
                .doOnSuccess(saved -> log.info("Added album photo {} for vetId={}", saved.getId(), vetId));
    }

    @Override
    public Mono<Album> insertAlbumPhoto(String vetId, String photoName, byte[] fileData) {
        return Mono.fromCallable(() -> {
                    if (fileData == null || fileData.length == 0) {
                        throw new InvalidInputException("Empty file data");
                    }
                    String contentType = determineContentType(photoName);
                    return Album.builder()
                            .vetId(vetId)
                            .filename(photoName)
                            .imgType(contentType)
                            .data(fileData)
                            .build();
                })
                .flatMap(albumRepository::save)
                .doOnSuccess(saved -> log.info("Added album photo {} for vetId={}", saved.getId(), vetId));
    }

    // Very small helper; default to JPEG if no extension match
    private String determineContentType(String filename) {
        if (filename == null) return "image/jpeg";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/jpeg";
    }
}



