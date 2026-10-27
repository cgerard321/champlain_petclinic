package com.petclinic.products.businesslayer.images;

import com.petclinic.products.domainclientlayer.FileRequestDTO;
import com.petclinic.products.domainclientlayer.FileResponseDTO;
import com.petclinic.products.domainclientlayer.FilesServiceClient;
import com.petclinic.products.utils.exceptions.FileNotFoundInFilesServiceException;
import com.petclinic.products.presentationlayer.images.ImageResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidImageTypeException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ImageServiceImpl implements ImageService{

    private final FilesServiceClient filesServiceClient;

    public ImageServiceImpl(FilesServiceClient filesServiceClient) {
        this.filesServiceClient = filesServiceClient;
    }

    @Override
    public Mono<ImageResponseModel> getImageByImageId(String imageId) {
        // Preserve the response contract used by CART; storage is exclusively Files Service.
        return filesServiceClient.getFile(imageId)
                .map(this::toResponse)
                .onErrorMap(FileNotFoundInFilesServiceException.class,
                        error -> new NotFoundException("Image id not found: " + imageId))
                .switchIfEmpty(Mono.error(new NotFoundException("Image id not found: " + imageId)));
    }

    private ImageResponseModel toResponse(FileResponseDTO file) {
        return ImageResponseModel.builder().imageId(file.getFileId())
                .imageName(file.getFileName()).imageType(file.getFileType())
                .imageData(file.getFileData()).build();
    }

    @Override
    public Mono<ImageResponseModel> addImage(String imageName, String imageType, FilePart imageData) {
        return EntityModelUtil.handleAddImage(imageName, imageType, imageData)
                .filter(imageRequestModel -> imageRequestModel.getImageType().equals("image/jpeg") ||
                        imageRequestModel.getImageType().equals("image/png") ||
                        imageRequestModel.getImageType().equals("image/jpg"))
                .switchIfEmpty(Mono.error(new InvalidImageTypeException("Image type not supported: " + imageType)))
                .flatMap(image -> filesServiceClient.addFile(FileRequestDTO.builder()
                        .fileName(image.getImageName()).fileType(image.getImageType())
                        .fileData(image.getImageData()).build()))
                .map(this::toResponse);
    }
}
