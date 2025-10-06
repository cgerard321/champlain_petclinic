package com.petclinic.vet.businesslayer.photos;


import com.petclinic.vet.dataaccesslayer.photos.Photo;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.presentationlayer.photos.PhotoRequestDTO;
import com.petclinic.vet.presentationlayer.photos.PhotoResponseDTO;
import com.petclinic.vet.utils.EntityDtoUtil;
import com.petclinic.vet.utils.exceptions.InvalidInputException;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;


import java.io.IOException;




@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoServiceImpl implements PhotoService {
    private final PhotoRepository photoRepository;

    @Override
    public Mono<PhotoResponseDTO> getPhotoByVetId(String vetId) {
        return photoRepository.findByVetId(vetId)
                .doOnSubscribe(subscription -> log.debug("Fetching photo for vetId: {}", vetId))
                .switchIfEmpty(Mono.error(new NotFoundException("Photo for vet " + vetId + " does not exist.")))
                .map(EntityDtoUtil::toPhotoResponseDTO)
                .doOnSuccess(photo -> log.info("Successfully fetched photo for vetId: {}", vetId))
                .doOnError(error -> log.error("Error fetching photo for vetId: {}", vetId, error));
    }

    @Override
    public Mono<PhotoResponseDTO> getDefaultPhotoByVetId(String vetId) {
        return photoRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .map(EntityDtoUtil::toPhotoResponseDTO);
    }


    @Override
    public Mono<PhotoResponseDTO> insertPhotoOfVet(String vetId, Mono<PhotoRequestDTO> photoRequestDTO) {
        return photoRequestDTO.flatMap(request -> {
                    if (request.getData() == null || request.getData().length == 0) {
                        return Mono.error(new InvalidInputException("Empty file data"));
                    }
                    
                    String contentType = request.getImgType();
                    if (contentType == null || contentType.isEmpty()) {
                        contentType = determineContentType(request.getFilename());
                    }
                    
                    Photo entity = Photo.builder()
                            .vetId(vetId)
                            .filename(request.getFilename())
                            .imgType(contentType)
                            .data(request.getData())
                            .build();
                    return photoRepository.save(entity);
                })
                .map(EntityDtoUtil::toPhotoResponseDTO);
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
    public Mono<PhotoResponseDTO> updatePhotoByVetId(String vetId, Mono<PhotoRequestDTO> photoRequestDTO) {
        return photoRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("Photo for vet " + vetId + " does not exist.")))
                .flatMap(existingPhoto -> photoRequestDTO.flatMap(request -> {
                            if (request.getData() == null || request.getData().length == 0) {
                                return Mono.error(new InvalidInputException("Empty file data"));
                            }
                            
                            String contentType = request.getImgType();
                            if (contentType == null || contentType.isEmpty()) {
                                contentType = determineContentType(request.getFilename());
                            }
                            
                            Photo updatedPhoto = Photo.builder()
                                    .id(existingPhoto.getId())
                                    .vetId(vetId)
                                    .filename(request.getFilename())
                                    .imgType(contentType)
                                    .data(request.getData())
                                    .build();
                            return photoRepository.save(updatedPhoto);
                        }))
                .map(EntityDtoUtil::toPhotoResponseDTO);
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

}
