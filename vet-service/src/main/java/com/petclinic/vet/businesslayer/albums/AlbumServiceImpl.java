package com.petclinic.vet.businesslayer.albums;

import com.petclinic.vet.dataaccesslayer.albums.Album;
import com.petclinic.vet.dataaccesslayer.albums.AlbumRepository;
import com.petclinic.vet.utils.exceptions.InvalidInputException;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService{

    private final AlbumRepository albumRepository;

    @Override
    public Flux<Album> getAllAlbumsByVetId(String vetId) {
        return albumRepository.findAllByVetId(vetId)  
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
    public Mono<Album> insertAlbumPhoto(String vetId, String photoName, Mono<byte[]> fileData) {
        return fileData
            .switchIfEmpty(Mono.error(new InvalidInputException("Empty file data")))
            .flatMap(bytes -> doInsert(vetId, photoName, bytes));
    }

    @Override
    public Mono<Album> insertAlbumPhoto(String vetId, String photoName, FilePart file) {
        return DataBufferUtils.join(file.content())
            .map(buf -> {
                byte[] bytes = new byte[buf.readableByteCount()];
                buf.read(bytes);
                DataBufferUtils.release(buf);
                return bytes;
            })
            .flatMap(bytes -> doInsert(vetId, photoName, bytes));
    }

    private Mono<Album> doInsert(String vetId, String photoName, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return Mono.error(new InvalidInputException("Empty file data"));
        }
        String contentType = determineContentType(photoName);

        Album toSave = Album.builder()
                .vetId(vetId)
                .filename(photoName)
                .imgType(contentType)
                .data(bytes)
                .build();

        return albumRepository.save(toSave)
                .doOnSuccess(saved -> log.info("Added album photo {} for vetId={}", saved.getId(), vetId));
    }

    private String determineContentType(String filename) {
        if (filename == null) return "image/jpeg";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp"))  return "image/bmp";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}