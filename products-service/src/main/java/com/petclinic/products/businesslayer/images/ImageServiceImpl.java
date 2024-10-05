package com.petclinic.products.businesslayer.images;

import com.petclinic.products.datalayer.images.ImageRepository;
import com.petclinic.products.presentationlayer.images.ImageResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidImageTypeException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.springframework.http.codec.multipart.FilePart;
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
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Image id not found: " + imageId))))
                .map(EntityModelUtil::toImageResponseModel);
    }

    @Override
    public Mono<ImageResponseModel> addImage(String imageName, String imageType, FilePart imageData) {
        return EntityModelUtil.handleAddImage(imageName, imageType, imageData)
//                .filter(imageRequestModel -> imageRequestModel.getImageData() != null)
                .filter(imageRequestModel -> imageRequestModel.getImageType().equals("image/jpeg") ||
                        imageRequestModel.getImageType().equals("image/png") ||
                        imageRequestModel.getImageType().equals("image/jpg"))
                .switchIfEmpty(Mono.error(new InvalidImageTypeException("Image type not supported: " + imageType)))
                .flatMap(imageRequestModel -> imageRepository.save(EntityModelUtil.toImageEntity(imageRequestModel))
                        .map(EntityModelUtil::toImageResponseModel));
    }
}
