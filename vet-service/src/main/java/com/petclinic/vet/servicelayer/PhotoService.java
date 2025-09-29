package com.petclinic.vet.servicelayer;

import com.petclinic.vet.presentationlayer.PhotoRequestDTO;
import com.petclinic.vet.presentationlayer.PhotoResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface PhotoService {
    Mono<PhotoResponseDTO> getPhotoByVetId(String vetId);
    Mono<PhotoResponseDTO> getDefaultPhotoByVetId(String vetId);
    Mono<PhotoResponseDTO> insertPhotoOfVet(String vetId, Mono<PhotoRequestDTO> photoRequestDTO);
    Mono<PhotoResponseDTO> updatePhotoByVetId(String vetId, Mono<PhotoRequestDTO> photoRequestDTO);
    Mono<Void> deletePhotoByVetId(String vetId);
}
