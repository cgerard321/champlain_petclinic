package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Products.ImageResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ImageServiceClient {

    private final WebClient webClient;
    private final WebClient.Builder webClientBuilder;
    private final String imageServiceUrl;

    public ImageServiceClient(WebClient.Builder webClientBuilder,
                              @Value("${app.products-service.host}") String productsServiceHost,
                              @Value("${app.products-service.port}") String productsServicePort) {
        this.webClientBuilder = webClientBuilder;
        imageServiceUrl = "http://" + productsServiceHost + ":" + productsServicePort + "/images";
        this.webClient = webClientBuilder
                .baseUrl(imageServiceUrl)
                .build();

    }

    public Mono<ImageResponseDTO> getImageByImageId(final String imageId) {
        return webClientBuilder.build()
                .get()
                .uri(imageServiceUrl + "/" + imageId)
                .retrieve()
                .bodyToMono(ImageResponseDTO.class);
    }

    public Mono<ImageResponseDTO> createImage(final String imageName, final String imageType,
                                              final FilePart imageData) {
        return webClientBuilder.build()
                .post()
                .uri(imageServiceUrl)
                .body(BodyInserters.fromMultipartData("imageName", imageName)
                        .with("imageType", imageType)
                        .with("imageData", imageData))
                .retrieve()
                .bodyToMono(ImageResponseDTO.class);
    }
}
