package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PromoCodeControllerUnitTest {

    private static final String BASE_URL = "/api/v2/gateway/promos";

    @Mock
    private CartServiceClient cartServiceClient;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PromoCodeController controller = new PromoCodeController(cartServiceClient);
        client = WebTestClient.bindToController(controller).build();
    }

    @Test
    void createAutomaticPromo_preservesEmployeeSettings() {
        PromoCodeRequestDTO request = sampleRequest();
        request.setAutomatic(true);
        request.setMinimumPurchaseAmount(100.0);
        PromoCodeResponseDTO response = samplePromo("automatic");
        response.setAutomatic(true);
        response.setMinimumPurchaseAmount(100.0);
        when(cartServiceClient.createPromoCode(any(PromoCodeRequestDTO.class)))
                .thenReturn(Mono.just(response));

        client.post().uri(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.automatic").isEqualTo(true)
                .jsonPath("$.minimumPurchaseAmount").isEqualTo(100.0);

        var captured = org.mockito.ArgumentCaptor.forClass(PromoCodeRequestDTO.class);
        verify(cartServiceClient).createPromoCode(captured.capture());
        assertThat(captured.getValue().getAutomatic()).isTrue();
        assertThat(captured.getValue().getMinimumPurchaseAmount()).isEqualTo(100.0);
    }

    private PromoCodeResponseDTO samplePromo(String id) {
        PromoCodeResponseDTO response = new PromoCodeResponseDTO();
        response.setId(id);
        response.setName("Summer Promo");
        response.setCode("SUMMER10");
        response.setDiscount(10.0);
        response.setActive(true);
        return response;
    }

    private PromoCodeRequestDTO sampleRequest() {
        return new PromoCodeRequestDTO("Summer Promo", "SUMMER10", 10.0, "2999-12-31T23:59:59", true);
    }

    @Test
    void getAllPromos_returnsPromos() {
        when(cartServiceClient.getAllPromoCodes()).thenReturn(Flux.just(samplePromo("p-1"), samplePromo("p-2")));

        Flux<PromoCodeResponseDTO> body = client.get()
                .uri(BASE_URL)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PromoCodeResponseDTO.class)
                .getResponseBody();

        StepVerifier.create(body)
                .expectNextCount(2)
                .verifyComplete();

        verify(cartServiceClient).getAllPromoCodes();
    }

    @Test
    void createPromoCode_returnsCreatedPromo() {
        when(cartServiceClient.createPromoCode(any(PromoCodeRequestDTO.class)))
                .thenReturn(Mono.just(samplePromo("p-1")));

        PromoCodeResponseDTO body = client.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PromoCodeResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUMMER10");
        verify(cartServiceClient).createPromoCode(any(PromoCodeRequestDTO.class));
    }

    @Test
    void createPromoCode_returnsNotFound_whenClientReturnsEmpty() {
        when(cartServiceClient.createPromoCode(any(PromoCodeRequestDTO.class))).thenReturn(Mono.empty());

        client.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updatePromoCode_returnsUpdatedPromo() {
        when(cartServiceClient.updatePromoCode(eq("p-1"), any(PromoCodeRequestDTO.class)))
                .thenReturn(Mono.just(samplePromo("p-1")));

        PromoCodeResponseDTO body = client.put()
                .uri(BASE_URL + "/{promoCodeId}", "p-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PromoCodeResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo("p-1");
        verify(cartServiceClient).updatePromoCode(eq("p-1"), any(PromoCodeRequestDTO.class));
    }

    @Test
    void updatePromoCode_returnsNotFound_whenClientReturnsEmpty() {
        when(cartServiceClient.updatePromoCode(eq("missing"), any(PromoCodeRequestDTO.class)))
                .thenReturn(Mono.empty());

        client.put()
                .uri(BASE_URL + "/{promoCodeId}", "missing")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleRequest())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getPromoCodeById_returnsPromo() {
        when(cartServiceClient.getPromoCodeById("p-1")).thenReturn(Mono.just(samplePromo("p-1")));

        PromoCodeResponseDTO body = client.get()
                .uri(BASE_URL + "/{promoCodeId}", "p-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PromoCodeResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo("p-1");
        verify(cartServiceClient).getPromoCodeById("p-1");
    }

    @Test
    void getPromoCodeById_returnsNotFound_whenClientReturnsEmpty() {
        when(cartServiceClient.getPromoCodeById("missing")).thenReturn(Mono.empty());

        client.get()
                .uri(BASE_URL + "/{promoCodeId}", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePromoById_returnsDeletedPromo() {
        when(cartServiceClient.deletePromoCode("p-1")).thenReturn(Mono.just(samplePromo("p-1")));

        client.delete()
                .uri(BASE_URL + "/{promoCodeId}", "p-1")
                .exchange()
                .expectStatus().isOk();

        verify(cartServiceClient).deletePromoCode("p-1");
    }

    @Test
    void deletePromoById_returnsNotFound_whenClientReturnsEmpty() {
        when(cartServiceClient.deletePromoCode("missing")).thenReturn(Mono.empty());

        client.delete()
                .uri(BASE_URL + "/{promoCodeId}", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getActivePromos_returnsActivePromos() {
        when(cartServiceClient.getActivePromos()).thenReturn(Flux.just(samplePromo("p-1")));

        client.get()
                .uri(BASE_URL + "/actives")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PromoCodeResponseDTO.class)
                .hasSize(1);

        verify(cartServiceClient).getActivePromos();
    }

    @Test
    void validatePromoCode_returnsPromo_whenValid() {
        when(cartServiceClient.validatePromoCode("SUMMER10")).thenReturn(Mono.just(samplePromo("p-1")));

        client.get()
                .uri(BASE_URL + "/validate/{promoCode}", "SUMMER10")
                .exchange()
                .expectStatus().isOk();

        verify(cartServiceClient).validatePromoCode("SUMMER10");
    }

    @Test
    void validatePromoCode_returnsBadRequest_whenInvalid() {
        when(cartServiceClient.validatePromoCode("BAD"))
                .thenReturn(Mono.error(new InvalidInputException("Promo code is not valid: BAD")));

        client.get()
                .uri(BASE_URL + "/validate/{promoCode}", "BAD")
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void imagePayloadIsForwardedAndReturned() {
        var request = sampleRequest();
        var image = com.petclinic.bffapigateway.dtos.Files.FileDetails.builder()
                .fileName("offer.png").fileType("image/png").fileData(new byte[]{1, 2, 3}).build();
        request.setImage(image);
        var response = samplePromo("p-1");
        response.setImage(image);
        response.setImageId("image-1");
        when(cartServiceClient.createPromoCode(any())).thenReturn(Mono.just(response));
        client.post().uri(BASE_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(request)
                .exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.image.fileData").isEqualTo("AQID")
                .jsonPath("$.imageId").isEqualTo("image-1");
        var capture = org.mockito.ArgumentCaptor.forClass(PromoCodeRequestDTO.class);
        verify(cartServiceClient).createPromoCode(capture.capture());
        assertThat(capture.getValue().getImage().getFileData()).containsExactly((byte) 1, (byte) 2, (byte) 3);
    }

    @Test
    void imageRemovalIsForwarded() {
        var request = sampleRequest();
        request.setRemoveImage(true);
        when(cartServiceClient.updatePromoCode(eq("p-1"), any())).thenReturn(Mono.just(samplePromo("p-1")));
        client.put().uri(BASE_URL + "/p-1").contentType(MediaType.APPLICATION_JSON).bodyValue(request)
                .exchange().expectStatus().isOk();
        var capture = org.mockito.ArgumentCaptor.forClass(PromoCodeRequestDTO.class);
        verify(cartServiceClient).updatePromoCode(eq("p-1"), capture.capture());
        assertThat(capture.getValue().isRemoveImage()).isTrue();
    }

    @Test
    void rejectedImageKeepsBadRequestStatus() {
        when(cartServiceClient.createPromoCode(any())).thenReturn(Mono.error(
                org.springframework.web.reactive.function.client.WebClientResponseException.create(
                        400, "Invalid image", org.springframework.http.HttpHeaders.EMPTY, new byte[0], null)));
        client.post().uri(BASE_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(sampleRequest())
                .exchange().expectStatus().isBadRequest();
    }

}
