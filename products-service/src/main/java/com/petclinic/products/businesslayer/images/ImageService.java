package com.petclinic.products.businesslayer.images;

import com.petclinic.products.presentationlayer.images.ImageResponseModel;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface ImageService {

    Mono<ImageResponseModel> getImageByImageId(String imageId);
    Mono<ImageResponseModel> addImage(String imageName, String imageType, FilePart imageData);
}
