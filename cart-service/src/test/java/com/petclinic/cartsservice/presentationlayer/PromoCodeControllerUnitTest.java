package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.PromoCodeService;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PromoCodeController.class)
public class PromoCodeControllerUnitTest {


    @MockBean
    private PromoCodeService promoCodeService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenCreateNewPromo_withValidPromoCodeRequest_thenReturnPromoCodeResponse() {
        // Arrange
        PromoCodeResponseModel expectedPromoCodeResponseModel = new PromoCodeResponseModel();
        expectedPromoCodeResponseModel.setId("12345");
        expectedPromoCodeResponseModel.setCode("PROMO2024");
        expectedPromoCodeResponseModel.setName("Discount Promo");
        expectedPromoCodeResponseModel.setActive(true);
        expectedPromoCodeResponseModel.setExpirationDate(LocalDateTime.now().plusDays(30));

        when(promoCodeService.createPromo(any(PromoCodeRequestModel.class)))
                .thenReturn(Mono.just(expectedPromoCodeResponseModel));

        String json = """
                  {
                    "name": "Discount Promo",
                    "code": "PROMO2024",
                    "discount": 10.0,
                    "expirationDate": "2024-12-31T23:59:59"
                  }
                """;

        // Act & Assert
        webTestClient
                .post()
                .uri("/api/v1/promos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(201)
                .expectBody()
                .jsonPath("$.code").isEqualTo("PROMO2024")
                .jsonPath("$.name").isEqualTo("Discount Promo");
    }

    @Test
    void whenCreateNewPromo_withInvalidDateFormat_thenReturnBadRequest() {
        // Arrange
        String invalidJson = """
                  {
                    "name": "Discount Promo",
                    "code": "PROMO2024",
                    "discount": 10.0,
                    "expirationDate": "2024-12-31"
                  }
                """;

        when(promoCodeService.createPromo(any(PromoCodeRequestModel.class)))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(400)));

        // Act & Assert
        webTestClient
                .post()
                .uri("/api/v1/promos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

    }
    @Test
    public void whenUpdatePromoCodeWithValidId_thenReturnUpdatedPromoCode() {
        PromoCodeRequestModel updatedRequest = PromoCodeRequestModel.builder()
                .Name("Updated Promo")
                .code("NEW2024")
                .discount(15.0)
                .expirationDate(LocalDateTime.now().plusDays(10).toString())
                .build();

        PromoCodeResponseModel updatedResponse = PromoCodeResponseModel.builder()
                .id("98f7b33a-d62a-420a-a84a-05a27c85fc91")  // Using a valid UUID
                .Name("Updated Promo")
                .code("NEW2024")
                .discount(15.0)
                .expirationDate(LocalDateTime.now().plusDays(10))
                .isActive(true)
                .build();

        when(promoCodeService.updatePromoCodeById(any(PromoCodeRequestModel.class), any(String.class)))
                .thenReturn(Mono.just(updatedResponse));

        webTestClient.put()
                .uri("/api/v1/promos/98f7b33a-d62a-420a-a84a-05a27c85fc91")  // Using a valid UUID here too
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PromoCodeResponseModel.class)
                .isEqualTo(updatedResponse);
    }
    @Test
    public void whenUpdatePromoCodeWithInvalidId_thenReturnUnprocessableEntity() {
        // Arrange: Create the request model with valid promo details
        PromoCodeRequestModel updatedRequest = PromoCodeRequestModel.builder()
                .Name("Updated Promo")
                .code("NEW2024")
                .discount(15.0)
                .expirationDate(LocalDateTime.now().plusDays(10).toString())
                .build();

        // When the service is called with an invalid promoCodeId, it should throw an exception
        when(promoCodeService.updatePromoCodeById(any(PromoCodeRequestModel.class), any(String.class)))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(422), "Provided promo code ID is invalid: invalid-id"));

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/promos/invalid-id")  // Using an invalid ID (non-UUID format)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)  // Correct way to assert 422 Unprocessable Entity
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided promo code ID is invalid: invalid-id");
    }

    @Test
    public void whenDeletePromoCodeWithValidId_thenReturnDeletedPromoCode() {
        // Arrange
        PromoCodeResponseModel deletedResponse = PromoCodeResponseModel.builder()
                .id("98f7b33a-d62a-420a-a84a-05a27c85fc91")  // Valid UUID
                .Name("Deleted Promo")
                .code("DELETED2024")
                .discount(10.0)
                .expirationDate(LocalDateTime.now().plusDays(30))
                .isActive(false)
                .build();

        when(promoCodeService.deletePromoCode("98f7b33a-d62a-420a-a84a-05a27c85fc91"))
                .thenReturn(Mono.just(deletedResponse));

        // Act & Assert
        webTestClient.delete()
                .uri("/api/v1/promos/98f7b33a-d62a-420a-a84a-05a27c85fc91")  // Valid ID
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK
                .expectBody(PromoCodeResponseModel.class)
                .isEqualTo(deletedResponse);
    }

    @Test
    public void whenDeletePromoCodeWithInvalidId_thenReturnUnprocessableEntity() {
        // Arrange
        when(promoCodeService.deletePromoCode("invalid-id"))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(422), "Provided promo code ID is invalid: invalid-id"));

        // Act & Assert
        webTestClient.delete()
                .uri("/api/v1/promos/invalid-id")  // Invalid ID
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)  // Expect 422 Unprocessable Entity
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided promo code ID is invalid: invalid-id");
    }

    @Test
    public void whenGetAllPromoCodes_thenReturnListOfPromoCodes() {
        // Arrange: Create a list of PromoCodeResponseModel objects
        PromoCodeResponseModel promo1 = PromoCodeResponseModel.builder()
                .id("promo1-id")
                .Name("Promo 1")
                .code("CODE1")
                .discount(10.0)
                .expirationDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        PromoCodeResponseModel promo2 = PromoCodeResponseModel.builder()
                .id("promo2-id")
                .Name("Promo 2")
                .code("CODE2")
                .discount(20.0)
                .expirationDate(LocalDateTime.now().plusDays(60))
                .isActive(true)
                .build();

        // Mocking the service to return a list of promo codes
        when(promoCodeService.getAllPromoCodes()).thenReturn(Flux.just(promo1, promo2));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/promos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK
                .expectBodyList(PromoCodeResponseModel.class)
                .hasSize(2)  // Expect two promo codes
                .contains(promo1, promo2);
    }

    @Test
    public void whenGetAllPromoCodes_thenReturnEmptyList() {
        // Arrange: Mocking the service to return an empty list
        when(promoCodeService.getAllPromoCodes()).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/promos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK
                .expectBodyList(PromoCodeResponseModel.class)
                .hasSize(0);  // Expect an empty list
    }

    @Test
    public void whenGetPromoCodeByIdWithValidId_thenReturnPromoCode() {
        // Arrange: Create a PromoCodeResponseModel object
        PromoCodeResponseModel promoResponse = PromoCodeResponseModel.builder()
                .id("98f7b33a-d62a-420a-a84a-05a27c85fc91")  // Valid UUID
                .Name("Promo 1")
                .code("CODE1")
                .discount(10.0)
                .expirationDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .build();

        // Mock the service to return a promo code
        when(promoCodeService.getPromoCodeById("98f7b33a-d62a-420a-a84a-05a27c85fc91"))
                .thenReturn(Mono.just(promoResponse));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/promos/98f7b33a-d62a-420a-a84a-05a27c85fc91")  // Valid ID
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK
                .expectBody(PromoCodeResponseModel.class)
                .isEqualTo(promoResponse);
    }
    @Test
    public void whenGetPromoCodeByIdWithInvalidId_thenReturnUnprocessableEntity() {
        // Arrange: Mock the service to throw a ResponseStatusException for invalid ID
        when(promoCodeService.getPromoCodeById("invalid-id"))
                .thenThrow(new ResponseStatusException(HttpStatusCode.valueOf(422), "Provided promo code ID is invalid: invalid-id"));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/promos/invalid-id")  // Invalid ID
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)  // Expect 422 Unprocessable Entity
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided promo code ID is invalid: invalid-id");
    }


    @Test
    public void whenGetPromoCodeByIdWithNonExistentId_thenReturnNotFound() {
        // Arrange: Mock the service to return an empty Mono for a non-existent ID
        when(promoCodeService.getPromoCodeById("98f7b33a-d62a-420a-a84a-05a27c85fc92"))
                .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/promos/98f7b33a-d62a-420a-a84a-05a27c85fc92")  // Non-existent ID
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();  // Expect 404 Not Found
    }

    @Test
    public void whenGetActivePromos_thenReturnListOfActivePromoCodes() {
        // Arrange: Crear una lista de PromoCodeResponseModel con promociones activas
        PromoCodeResponseModel activePromo1 = PromoCodeResponseModel.builder()
                .id("promo1-id")
                .Name("Active Promo 1")
                .code("ACTIVE1")
                .discount(15.0)
                .expirationDate(LocalDateTime.now().plusDays(10))
                .isActive(true)
                .build();

        PromoCodeResponseModel activePromo2 = PromoCodeResponseModel.builder()
                .id("promo2-id")
                .Name("Active Promo 2")
                .code("ACTIVE2")
                .discount(20.0)
                .expirationDate(LocalDateTime.now().plusDays(20))
                .isActive(true)
                .build();

        // Mocking the service to return the list of active promo codes
        when(promoCodeService.getActivePromos()).thenReturn(Flux.just(activePromo1, activePromo2));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/promos/actives")  // URI para obtener promociones activas
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Expectar 200 OK
                .expectBodyList(PromoCodeResponseModel.class)
                .hasSize(2)  // Esperamos dos promociones activas
                .contains(activePromo1, activePromo2);  // Verificar que ambas promociones activas están presentes
    }

    @Test
    public void whenGetActivePromos_thenReturnEmptyList() {
        // Arrange: Simular que no hay promociones activas
        when(promoCodeService.getActivePromos()).thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/promos/actives")  // URI para obtener promociones activas
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()  // Expectar 200 OK
                .expectBodyList(PromoCodeResponseModel.class)
                .hasSize(0);  // Esperamos una lista vacía
    }

//    @Test
//    void whenValidatePromoCode_withInvalidCode_thenReturnBadRequest() {
//        // Arrange
//        String promoCode = "INVALIDCODE";
//        when(promoCodeService.getPromoCodeByCode(promoCode)).thenReturn(Mono.empty());
//
//        // Act & Assert
//        webTestClient.get()
//                .uri("/api/v1/promos/validate/{promoCode}", promoCode)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Promo code is not valid");
//    }
//
//
//    @Test
//    void whenValidatePromoCode_withInactiveCode_thenReturnBadRequest() {
//        // Arrange
//        String promoCode = "INACTIVECODE";
//        PromoCodeResponseModel promo = new PromoCodeResponseModel();
//        promo.setId("67890");
//        promo.setCode(promoCode);
//        promo.setName("Inactive Promo");
//        promo.setActive(false);
//        promo.setExpirationDate(LocalDateTime.now().plusDays(30));
//
//        when(promoCodeService.getPromoCodeByCode(promoCode)).thenReturn(Mono.just(promo));
//
//        // Act & Assert
//        webTestClient.get()
//                .uri("/api/v1/promos/validate/{promoCode}", promoCode)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Promo code is not valid");
//    }
//
//








}
