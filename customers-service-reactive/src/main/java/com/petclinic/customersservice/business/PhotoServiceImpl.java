package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Photo;
import com.petclinic.customersservice.data.PhotoRepo;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.data.PetTypeRepo;
import com.petclinic.customersservice.presentationlayer.PhotoResponseModel;
import com.petclinic.customersservice.util.EntityDTOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Base64;

@Service
@Slf4j
public class PhotoServiceImpl implements PhotoService {

    @Autowired
    private PhotoRepo photoRepo;

    @Autowired
    private PetRepo petRepo;

    @Autowired
    private PetTypeRepo petTypeRepo;

    @Override
    public Mono<PhotoResponseModel> getPetPhotoByPetId(String petId) {
        log.info("Getting photo for pet with id: {}", petId);

        return petRepo.findPetByPetId(petId)
                .switchIfEmpty(Mono.error(new NotFoundException("Pet not found with id: " + petId)))
                .flatMap(pet -> {
                    String photoId = pet.getPhotoId();
                    if (photoId != null && !photoId.isEmpty()) {
                        return photoRepo.findById(photoId)
                                .map(EntityDTOUtil::toPhotoResponseModel)
                                .switchIfEmpty(loadDefaultPetTypeImage(pet.getPetTypeId()));
                    }
                    return loadDefaultPetTypeImage(pet.getPetTypeId());
                });
    }

    @Override
    public Mono<Photo> insertPhoto(Mono<Photo> photoMono) {
        return photoMono.flatMap(photo -> {
            log.info("Inserting photo with id: {}", photo.getId());
            return photoRepo.save(photo);
        });
    }

    @Override
    public Mono<Photo> getPhotoByPhotoId(String photoId) {
        log.info("Getting photo with id: {}", photoId);
        return photoRepo.findById(photoId);
    }

    @Override
    public Mono<Void> deletePhotoByPhotoId(String photoId) {
        log.info("Deleting photo with id: {}", photoId);
        return photoRepo.deleteById(photoId);
    }

    private Mono<PhotoResponseModel> loadDefaultPetTypeImage(String petTypeId) {
        return petTypeRepo.findPetTypeById(petTypeId)
                .flatMap(petType -> Mono.fromCallable(() -> {
                    try {
                        String imageName = petType.getName().toLowerCase() + ".jpg";
                        ClassPathResource resource = new ClassPathResource("images/" + imageName);

                        if (!resource.exists()) {
                            log.warn("Image not found for pet type: {}, using default cat.jpg", petType.getName());
                            imageName = "cat.jpg";
                            resource = new ClassPathResource("images/" + imageName);
                        }

                        byte[] imageBytes = new byte[0];
                        if (resource.exists()) {
                            imageBytes = StreamUtils.copyToByteArray(resource.getInputStream());
                        } else {
                            log.error("Default fallback image not found, returning empty image");
                        }

                        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                        Photo photo = Photo.builder()
                                .id(petTypeId)
                                .name(petType.getName() + " default photo")
                                .type("image/jpeg")
                                .photo(base64Image)
                                .build();

                        return EntityDTOUtil.toPhotoResponseModel(photo);

                    } catch (IOException e) {
                        log.error("Error loading default image for pet type: {}", petType.getName(), e);
                        Photo errorPhoto = Photo.builder()
                                .id("0") 
                                .name("Error loading photo")
                                .type("image/jpeg")
                                .photo("")
                                .build();
                        return EntityDTOUtil.toPhotoResponseModel(errorPhoto);
                    }
                }))
                .switchIfEmpty(Mono.fromCallable(() -> {
                    log.error("Pet type not found with id: {}", petTypeId);
                    Photo errorPhoto = Photo.builder()
                            .id("0") 
                            .name("Pet type not found")
                            .type("image/jpeg")
                            .photo("")
                            .build();
                    return EntityDTOUtil.toPhotoResponseModel(errorPhoto);
                }));
    }
}
