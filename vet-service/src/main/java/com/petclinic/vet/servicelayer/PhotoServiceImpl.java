package com.petclinic.vet.servicelayer;


import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.exceptions.NotFoundException;
import com.petclinic.vet.presentationlayer.PhotoResponseDTO;
import com.petclinic.vet.util.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.io.IOException;




@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoServiceImpl implements PhotoService {
    private final PhotoRepository photoRepository;

    @Override
    public Mono<Resource> getPhotoByVetId(String vetId) {
        return photoRepository.findByVetId(vetId)
                .doOnSubscribe(subscription -> log.debug("Fetching photo for vetId: {}", vetId))
                .switchIfEmpty(Mono.error(new NotFoundException("Photo for vet " + vetId + " does not exist.")))
                .map(this::createResourceFromPhoto)
                .cast(Resource.class) // Explicit cast to Resource
                .doOnSuccess(resource -> log.info("Successfully fetched photo for vetId: {}", vetId))
                .doOnError(error -> log.error("Error fetching photo for vetId: {}", vetId, error));
    }

    @Override
    public Mono<PhotoResponseDTO> getDefaultPhotoByVetId(String vetId) {
        return photoRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .map(EntityDtoUtil::toPhotoResponseDTO);
    }


    @Override
    public Mono<Resource> insertPhotoOfVet(String vetId, String photoName, MultipartFile file) {
        return Mono.fromCallable(() -> {
                    if (file == null || file.getSize() <= 0) {
                        throw new InvalidInputException("Empty file");
                    }
                    String ct = file.getContentType();
                    if (ct == null || !ct.startsWith("image/")) {
                        throw new InvalidInputException("Unsupported media type");
                    }
                    Photo entity = Photo.builder()
                            .vetId(vetId)
                            .filename(photoName)
                            .imgType(ct)
                            .data(file.getBytes())
                            .build();
                    return entity;
                })
                .flatMap(photoRepository::save)
                .map(saved -> new ByteArrayResource(saved.getData()));
    }

    @Override
    public Mono<Resource> insertPhotoOfVet(String vetId, String photoName, byte[] fileData) {
        return Mono.fromCallable(() -> {
                    if (fileData == null || fileData.length == 0) {
                        throw new InvalidInputException("Empty file data");
                    }
                    
                    // Determine content type based on file extension or default to JPEG
                    String contentType = determineContentType(photoName);
                    
                    Photo entity = Photo.builder()
                            .vetId(vetId)
                            .filename(photoName)
                            .imgType(contentType)
                            .data(fileData)
                            .build();
                    return entity;
                })
                .flatMap(photoRepository::save)
                .map(saved -> new ByteArrayResource(saved.getData()));
    }

    private String determineContentType(String filename) {
        if (filename == null) {
            return "image/jpeg";
        }
        String lowerCase = filename.toLowerCase();
        if (lowerCase.endsWith(".png")) {
            return "image/png";
        } else if (lowerCase.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerCase.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "image/jpeg"; // Default to JPEG
        }
    }



    @Override
    public Mono<Resource> updatePhotoByVetId(String vetId, String photoName, Mono<Resource> photo) {
        return photoRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("Photo for vet " + vetId + " does not exist.")))
                .flatMap(existingPhoto -> photo.map(resource -> {
                            Photo updatedPhoto = EntityDtoUtil.toPhotoEntity(
                                    vetId, photoName, resource);
                            updatedPhoto.setId(existingPhoto.getId());
                            return updatedPhoto;
                        })
                        .flatMap(updatedPhoto -> {
                            return photoRepository.save(updatedPhoto)
                                    .map(savedPhoto -> {
                                        return new ByteArrayResource(savedPhoto.getData());
                                    });
                        }));
    }

    @Override
    public Mono<Void> deletePhotoByVetId(String vetId) {
        return photoRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new InvalidInputException("Photo not found for vetId: " + vetId)))
                .flatMap(photo -> photoRepository.deleteByVetId(vetId))
                .then(insertDefaultPhoto(vetId))
                .then();
    }

    private Mono<Void> insertDefaultPhoto(String vetId) {
        return Mono.defer(() -> {
            try {
                ClassPathResource defaultPhoto = new ClassPathResource("images/vet_default.jpg");
                byte[] data = StreamUtils.copyToByteArray(defaultPhoto.getInputStream());

                Photo defaultPhotoEntity = Photo.builder()
                        .vetId(vetId)
                        .filename("vet_default.jpg")
                        .imgType("image/jpeg")
                        .data(data)
                        .build();

                return photoRepository.save(defaultPhotoEntity).then();

            } catch (IOException e) {
                return Mono.error(new RuntimeException("Failed to load default photo", e));
            }
        });
    }


    private ByteArrayResource createResourceFromPhoto(Photo img) {
        return new ByteArrayResource(img.getData());
    }

}
