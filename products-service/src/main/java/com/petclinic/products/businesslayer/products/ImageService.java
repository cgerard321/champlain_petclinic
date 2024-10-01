package com.petclinic.products.businesslayer.products;

import com.petclinic.products.presentationlayer.images.ImageRequestModel;
import com.petclinic.products.presentationlayer.images.ImageResponseModel;
import reactor.core.publisher.Mono;

public interface ImageService {

    Mono<ImageResponseModel> getImageByImageId(String imageId);
    Mono<ImageResponseModel> addImage(Mono<ImageRequestModel> imageRequestModel);
}
