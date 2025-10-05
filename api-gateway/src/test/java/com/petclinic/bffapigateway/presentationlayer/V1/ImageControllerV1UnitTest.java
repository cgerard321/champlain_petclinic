package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.ImageServiceClient;
import com.petclinic.bffapigateway.dtos.Products.ImageResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.ImageControllerV1;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        ImageControllerV1.class,
        ImageServiceClient.class,
        GlobalExceptionHandler.class
})
@WebFluxTest(controllers = ImageControllerV1.class)
@AutoConfigureWebTestClient
class ImageControllerV1UnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ImageServiceClient imageServiceClient;

    private final String baseImageURL = "/api/gateway/images";
    private String imageName = "testImage";
    private String imageType = "image/jpeg";

    @Test
    public void whenGetImage_thenReturnImageResponse() {
        ImageResponseDTO expectedImageResponse = new ImageResponseDTO();

        when(imageServiceClient.getImageByImageId(anyString()))
                .thenReturn(Mono.just(expectedImageResponse));

        webTestClient.get()
                .uri(baseImageURL + "/{imageId}", "imageId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ImageResponseDTO.class)
                .isEqualTo(expectedImageResponse);

        verify(imageServiceClient, times(1)).getImageByImageId(eq("imageId"));
    }

    @Test
    public void whenAddImage_thenReturnImageResponse() {
        byte[] imageData = "sampleImageData".getBytes();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("imageData", "testImage.jpg",
                "image/jpeg", imageData);

        ImageResponseDTO expectedImageResponse = new ImageResponseDTO();

        when(imageServiceClient.createImage(anyString(), anyString(), any()))
                .thenReturn(Mono.just(expectedImageResponse));

        webTestClient.post()
                .uri(baseImageURL)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("imageName", imageName)
                        .with("imageType", imageType)
                        .with("imageData", mockMultipartFile.getResource()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ImageResponseDTO.class)
                .isEqualTo(expectedImageResponse);

        verify(imageServiceClient, times(1)).createImage(eq(imageName), eq(imageType), any());
    }
}