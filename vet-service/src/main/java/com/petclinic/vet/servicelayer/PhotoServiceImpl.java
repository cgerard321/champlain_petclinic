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
                .switchIfEmpty(Mono.error(new NotFoundException("Photo for vet " + vetId + " does not exist.")))
                .map(img -> {
                    byte[] decodedImg = decodeFromBase64(img.getImgBase64());  //decode the Base64 data back to byte[]
                    ByteArrayResource resource = new ByteArrayResource(decodedImg);
                    return resource;
                });
    }


    @Override
    public Mono<PhotoResponseDTO> getDefaultPhotoByVetId(String vetId) {
        return photoRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("vetId not found: " + vetId)))
                .map(EntityDtoUtil::toPhotoResponseDTO);
    }


    @Override
    public Mono<Resource> insertPhotoOfVet(String vetId, String photoName, Mono<Resource> photo) {
        return photo
                .map(p -> EntityDtoUtil.toPhotoEntity(vetId, photoName, p))  //convert to Photo entity
                .flatMap(photoRepository::save)
                .map(img -> {
                    byte[] decodedImg = decodeFromBase64(img.getImgBase64());  //decode Base64 to byte[]
                    ByteArrayResource resource = new ByteArrayResource(decodedImg);
                    return resource;
                });
    }



    @Override
    public Mono<Resource> updatePhotoByVetId(String vetId, String photoName, Mono<Resource> photo) {
        return photoRepository.findByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("Photo for vet " + vetId + " does not exist.")))
                .flatMap(existingPhoto -> photo.map(resource -> {
                            Photo updatedPhoto = EntityDtoUtil.toPhotoEntity(vetId, photoName, resource);
                            updatedPhoto.setId(existingPhoto.getId());
                            return updatedPhoto;
                        })
                        .flatMap(updatedPhoto -> {
                            return photoRepository.save(updatedPhoto)
                                    .map(savedPhoto -> {
                                        byte[] decodedImg = decodeFromBase64(savedPhoto.getImgBase64());  //decode Base64 to byte[]
                                        ByteArrayResource savedResource = new ByteArrayResource(decodedImg);
                                        return savedResource;
                                    });
                        }));
    }

    //helper method to decode Base64 string to byte[]
    private byte[] decodeFromBase64(String base64Data) {
        return java.util.Base64.getDecoder().decode(base64Data);
    }

}
