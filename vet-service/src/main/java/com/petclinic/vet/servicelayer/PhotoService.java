package com.petclinic.vet.servicelayer;


import com.petclinic.vet.presentationlayer.PhotoResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;


public interface PhotoService {
    Mono<Resource> getPhotoByVetId(String vetId);
    Mono<PhotoResponseDTO> getDefaultPhotoByVetId(String vetId);
    Mono<Resource> insertPhotoOfVet(String vetId, String photoName, Mono<Resource> photo);
    Mono<Resource> updatePhotoByVetId(String vetId, String photoName, Mono<Resource> photo);
   // Mono<Resource> insertPhotoOfVet(String vetId, String photoName, MultipartFile photo);
    Mono<Void> deletePhotoByVetId(String vetId);


}
