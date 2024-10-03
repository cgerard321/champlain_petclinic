package com.petclinic.products.businesslayer.products;

import com.petclinic.products.datalayer.products.ImageRepository;
import com.petclinic.products.presentationlayer.images.ImageRequestModel;
import com.petclinic.products.presentationlayer.images.ImageResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import org.springframework.core.io.buffer.DataBufferUtils;
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
                .map(EntityModelUtil::toImageResponseModel);
    }

    @Override
    public Mono<ImageResponseModel> addImage(String imageName, String imageType, FilePart imageData) {
        return EntityModelUtil.handleAddImage(imageName, imageType, imageData)
                .flatMap(imageRequestModel -> imageRepository.save(EntityModelUtil.toImageEntity(imageRequestModel))
                        .map(EntityModelUtil::toImageResponseModel));

//    public Mono<ImageResponseModel> handleAddImage(String imageName, String imageType, FilePart imageData) {
//        return imageData.content()
//                .reduce(new byte[0], (accumulated, buffer) -> {
//                    byte[] newBytes = new byte[accumulated.length + buffer.readableByteCount()];
//                    System.arraycopy(accumulated, 0, newBytes, 0, accumulated.length);
//                    buffer.read(newBytes, accumulated.length, buffer.readableByteCount());
//                    DataBufferUtils.release(buffer);
//                    return newBytes;
//                })
//                .flatMap(imageDataBytes -> {
//                    ImageRequestModel imageRequestModel = new ImageRequestModel();
//                    imageRequestModel.setImageName(imageName);
//                    imageRequestModel.setImageType(imageType);
//                    imageRequestModel.setImageData(imageDataBytes);
//
//                    return addImage(Mono.just(imageRequestModel));
//                });
    }
}
