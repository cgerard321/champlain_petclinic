package com.petclinic.products.presentationlayer.images;

import com.petclinic.products.businesslayer.products.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping(value = "{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ImageResponseModel>> getImage(@PathVariable String imageId) {
        return Mono.just(imageId)
                .flatMap(imageService::getImageByImageId)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ImageResponseModel>> addImage(@RequestPart("imageName") String imageName,
                                                             @RequestPart("imageType") String imageType,
                                                             @RequestPart("imageData") FilePart imageData) {
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

        return imageService.addImage(imageName, imageType, imageData)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }
}
