package com.petclinic.products.presentationlayer.images;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.datalayer.images.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=0"})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ImageControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ImageRepository imageRepository;

    private String baseURI = "/images";

    private final Image image1 = Image.builder()
            .imageId("c76ed4c1-fc5d-4868-8b39-1bca6b0be368")
            .imageName("Fish Tank Heater")
            .imageType("image/png")
            .imageData(Files.readAllBytes(Paths.get("src/main/resources/images/fish_tank_heater.png")))
            .build();

    private final Image image2 = Image.builder()
            .imageId(UUID.randomUUID().toString())
            .imageName("test")
            .imageType("image/jpeg")
            .imageData("dummy_image_data2".getBytes())
            .build();

    private final String imageName = "dog_food.png";
    private final String invalidImageName = "invalid_image.gif";
    private final String imageTypeRequestJPEG = "image/jpeg";
    private final String imageTypeRequestJPG = "image/jpg";
    private final String imageTypeRequestPNG = "image/png";
    private final String invalidImageTypeRequest = "image/gif";
    private final byte[] imageBytes = Files.readAllBytes(Paths.get("src/main/resources/images/dog_food.png"));

    ImageControllerIntegrationTest() throws IOException {
    }

    @BeforeEach
    public void setupDB() {
        Publisher<Image> setupDB = imageRepository.deleteAll()
                .thenMany(Flux.just(image1, image2)
                        .flatMap(imageRepository::save));

        StepVerifier
                .create(setupDB)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenGetImageById_thenReturnImageResponseModel() {
        webTestClient
                .get()
                .uri(baseURI + "/" + image1.getImageId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ImageResponseModel.class)
                .value(imageResponseModel -> {
                    assertNotNull(imageResponseModel);
                    assertEquals(image1.getImageId(), imageResponseModel.getImageId());
                    assertEquals(image1.getImageName(), imageResponseModel.getImageName());
                    assertEquals(image1.getImageType(), imageResponseModel.getImageType());
                    assertArrayEquals(image1.getImageData(), imageResponseModel.getImageData());
                });
    }

    @Test
    public void whenGetImageWithNonExistentId_thenReturnException() {
        webTestClient
                .get()
                .uri(baseURI + "/" + "ebd0c33e-86c7-4de7-9dc6-cbd413c91d57")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Image id not found: " +
                        "ebd0c33e-86c7-4de7-9dc6-cbd413c91d57");
    }

    @Test
    public void whenGetImageWithInvalidId_thenReturnException() {
        webTestClient
                .get()
                .uri(baseURI + "/" + "invalid_id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided image id is invalid: invalid_id");
    }

    @Test
    public void whenAddJPEG_thenReturnImageResponseModel() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                imageName,
                imageTypeRequestJPEG,
                imageBytes
        );

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
                .header("Content-Disposition", "form-data; name=imageData; filename=" +
                        file.getOriginalFilename());
        builder.part("imageName", imageName);
        builder.part("imageType", imageTypeRequestJPEG);

        webTestClient.post()
                .uri(baseURI)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ImageResponseModel.class)
                .value(imageResponseModel -> {
                    assertNotNull(imageResponseModel);
                    assertEquals(imageName, imageResponseModel.getImageName());
                    assertEquals(imageTypeRequestJPEG, imageResponseModel.getImageType());
                    assertArrayEquals(imageBytes, imageResponseModel.getImageData());
                });
    }

    @Test
    public void whenAddJPG_thenReturnImageResponseModel() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                imageName,
                imageTypeRequestJPG,
                imageBytes
        );

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
                .header("Content-Disposition", "form-data; name=imageData; filename=" +
                        file.getOriginalFilename());
        builder.part("imageName", imageName);
        builder.part("imageType", imageTypeRequestJPG);

        webTestClient.post()
                .uri(baseURI)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ImageResponseModel.class)
                .value(imageResponseModel -> {
                    assertNotNull(imageResponseModel);
                    assertEquals(imageName, imageResponseModel.getImageName());
                    assertEquals(imageTypeRequestJPG, imageResponseModel.getImageType());
                    assertArrayEquals(imageBytes, imageResponseModel.getImageData());
                });
    }

    @Test
    public void whenAddPNG_thenReturnImageResponseModel() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                imageName,
                imageTypeRequestPNG,
                imageBytes
        );

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
                .header("Content-Disposition", "form-data; name=imageData; filename=" +
                        file.getOriginalFilename());
        builder.part("imageName", imageName);
        builder.part("imageType", imageTypeRequestPNG);

        webTestClient.post()
                .uri(baseURI)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ImageResponseModel.class)
                .value(imageResponseModel -> {
                    assertNotNull(imageResponseModel);
                    assertEquals(imageName, imageResponseModel.getImageName());
                    assertEquals(imageTypeRequestPNG, imageResponseModel.getImageType());
                    assertArrayEquals(imageBytes, imageResponseModel.getImageData());
                });
    }

    @Test
    public void whenAddImageWithInvalidImageType_thenReturnException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                invalidImageName,
                invalidImageTypeRequest,
                imageBytes
        );

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
                .header("Content-Disposition", "form-data; name=imageData; filename=" +
                        file.getOriginalFilename());
        builder.part("imageName", imageName);
        builder.part("imageType", invalidImageTypeRequest);

        webTestClient.post()
                .uri(baseURI)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Image type not supported: " +
                        invalidImageTypeRequest);
    }
}