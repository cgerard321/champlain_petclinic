package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.ImageServiceClient;
import com.petclinic.bffapigateway.dtos.Products.ImageResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gateway/images")
@Validated
public class ImageControllerV1 {

    private final ImageServiceClient imageServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "{imageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ImageResponseDTO>> getImage(@PathVariable String imageId) {
        return Mono.just(imageId)
                .flatMap(imageServiceClient::getImageByImageId)
                .map(ResponseEntity::ok);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ImageResponseDTO>> addImage(@RequestPart("imageName") String imageName,
                                                           @RequestPart("imageType") String imageType,
                                                           @RequestPart("imageData") FilePart imageData) {
        return imageServiceClient.createImage(imageName, imageType, imageData)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }
}
