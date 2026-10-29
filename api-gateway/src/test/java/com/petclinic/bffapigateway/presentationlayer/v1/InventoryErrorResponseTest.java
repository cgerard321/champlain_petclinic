package com.petclinic.bffapigateway.presentationlayer.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.InventoryServiceClient;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputsInventoryException;
import com.petclinic.bffapigateway.exceptions.InventoryNotFoundException;
import com.petclinic.bffapigateway.exceptions.ProductListNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryErrorResponseTest {
    private InventoryServiceClient inventoryServiceClient;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        inventoryServiceClient = mock(InventoryServiceClient.class);
        client = WebTestClient.bindToController(new InventoryControllerV1(inventoryServiceClient))
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void missingInventoryReturns404WithMessage() {
        when(inventoryServiceClient.getInventoryById("missing"))
                .thenReturn(Mono.error(new InventoryNotFoundException(
                        "Inventory does not exist", HttpStatus.NOT_FOUND)));

        client.get().uri("/api/gateway/inventories/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.message").isEqualTo("Inventory does not exist");
    }

    @Test
    void duplicateFrenchInventoryNameReturns422WithMessage() {
        String id = "12345678-1234-1234-1234-123456789012";
        when(inventoryServiceClient.updateInventory(any(InventoryRequestDTO.class), eq(id)))
                .thenReturn(Mono.error(new InvalidInputsInventoryException(
                        "French inventory name already exists", HttpStatus.UNPROCESSABLE_ENTITY)));

        client.put().uri("/api/gateway/inventories/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(InventoryRequestDTO.builder()
                        .name(Map.of("en", "Bandages", "fr", "Bandages français"))
                        .description(Map.of("en", "Wound care", "fr", "Soins des plaies"))
                        .build())
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.message").isEqualTo("French inventory name already exists");
    }

    @Test
    void missingSupplyListReturns404WithMessage() {
        when(inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductsField(
                eq("inv1"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Flux.error(new ProductListNotFoundException(
                        "No supplies", HttpStatus.NOT_FOUND)));

        Flux<JsonNode> errors = client.get().uri("/api/gateway/inventories/inv1/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isNotFound()
                .returnResult(JsonNode.class).getResponseBody();

        StepVerifier.create(errors)
                .assertNext(error -> {
                    assertEquals(404, error.path("statusCode").asInt());
                    assertEquals("No supplies", error.path("message").asText());
                })
                .expectComplete().verify(Duration.ofSeconds(5));
        verify(inventoryServiceClient).getProductsInInventoryByInventoryIdAndProductsField(
                "inv1", null, null, null, null, null, null);
    }
}
