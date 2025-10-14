package com.petclinic.products.presentationlayer.images;

import com.petclinic.products.businesslayer.images.ImageService;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping(value = "{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ImageResponseModel>> getImage(@PathVariable String imageId) {
        return Mono.just(imageId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided image id is invalid: " + imageId)))
                .flatMap(imageService::getImageByImageId)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ImageResponseModel>> addImage(@RequestPart("imageName") String imageName,
                                                             @RequestPart("imageType") String imageType,
                                                             @RequestPart("imageData") FilePart imageData) {
        return imageService.addImage(imageName, imageType, imageData)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }
}
