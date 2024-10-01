package com.petclinic.products.businesslayer.products;

import com.petclinic.products.datalayer.products.ImageRepository;
import com.petclinic.products.presentationlayer.images.ImageRequestModel;
import com.petclinic.products.presentationlayer.images.ImageResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ImageServiceImpl implements ImageService{

    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Mono<ImageResponseModel> getImageByImageId(String imageId) {
        return imageRepository.findImageByImageId(imageId)
                .map(EntityModelUtil::toImageResponseModel);
    }

    @Override
    public Mono<ImageResponseModel> addImage(Mono<ImageRequestModel> imageRequestModel) {
        return imageRequestModel
                .map(EntityModelUtil::toImageEntity)
                .flatMap(imageRepository::save)
                .map(EntityModelUtil::toImageResponseModel);
    }
}
