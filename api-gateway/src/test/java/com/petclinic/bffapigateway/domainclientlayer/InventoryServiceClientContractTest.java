package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductRequestDTO;
import com.petclinic.bffapigateway.exceptions.InventoryNotFoundException;
import com.petclinic.bffapigateway.utils.Rethrower;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryServiceClientContractTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String PRODUCT_RESPONSE = """
            {"productId":"p1","inventoryId":"inv1","name":{"en":"Gauze","fr":"Gaze"},
             "description":{"en":"Sterile","fr":"Stérile"}}
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockWebServer mockWebServer;
    private InventoryServiceClient inventoryServiceClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        inventoryServiceClient = new InventoryServiceClient("localhost", String.valueOf(mockWebServer.getPort()));
        ReflectionTestUtils.setField(inventoryServiceClient, "rethrower", new Rethrower(objectMapper));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void addInventoryPreservesBothTranslationsInRequestAndResponse() throws Exception {
        InventoryRequestDTO request = InventoryRequestDTO.builder()
                .inventoryType("Internal")
                .name(Map.of("en", "Bandages", "fr", "Pansements"))
                .description(Map.of("en", "Wound care", "fr", "Soins des plaies"))
                .build();
        mockWebServer.enqueue(jsonResponse("""
                {"inventoryId":"inv1","name":{"en":"Bandages","fr":"Pansements"},
                 "description":{"en":"Wound care","fr":"Soins des plaies"}}
                """));

        StepVerifier.create(inventoryServiceClient.addInventory(request))
                .assertNext(response -> {
                    assertEquals(request.getName(), response.getName());
                    assertEquals(request.getDescription(), response.getDescription());
                })
                .expectComplete().verify(TIMEOUT);

        RecordedRequest sent = takeRequest("POST", "/inventory");
        JsonNode body = objectMapper.readTree(sent.getBody().readUtf8());
        assertEquals(objectMapper.valueToTree(request.getName()), body.path("name"));
        assertEquals(objectMapper.valueToTree(request.getDescription()), body.path("description"));
    }

    @Test
    void updateSupplyPreservesBothTranslationsAndActor() throws Exception {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .name(Map.of("en", "Gauze", "fr", "Gaze"))
                .description(Map.of("en", "Sterile", "fr", "Stérile"))
                .build();
        mockWebServer.enqueue(jsonResponse(PRODUCT_RESPONSE));

        StepVerifier.create(inventoryServiceClient.updateProductInInventory(request, "inv1", "p1", "manager"))
                .assertNext(response -> {
                    assertEquals(request.getName(), response.getName());
                    assertEquals(request.getDescription(), response.getDescription());
                })
                .expectComplete().verify(TIMEOUT);

        RecordedRequest sent = takeRequest("PUT", "/inventory/inv1/products/p1");
        assertEquals("manager", sent.getHeader("X-Actor-Username"));
        JsonNode body = objectMapper.readTree(sent.getBody().readUtf8());
        assertEquals(objectMapper.valueToTree(request.getName()), body.path("name"));
        assertEquals(objectMapper.valueToTree(request.getDescription()), body.path("description"));
    }

    @Test
    void supplyHistoryPreservesAuditDetails() throws InterruptedException {
        mockWebServer.enqueue(jsonResponse("""
                [{"inventoryId":"inv1","supplyId":"p1","supplyName":"Gaze",
                  "previousQuantity":10,"newQuantity":9,"quantityChange":-1,
                  "action":"CONSUMED","changedBy":"manager"}]
                """));

        StepVerifier.create(inventoryServiceClient.getSupplyHistory("inv1", "p1"))
                .assertNext(event -> {
                    assertEquals("p1", event.get("supplyId"));
                    assertEquals("Gaze", event.get("supplyName"));
                    assertEquals(-1, event.get("quantityChange"));
                    assertEquals("manager", event.get("changedBy"));
                })
                .expectComplete().verify(TIMEOUT);
        takeRequest("GET", "/inventory/inv1/products/p1/history");
    }

    @Test
    void missingSupplyHistoryPreservesServiceErrorMessage() throws InterruptedException {
        mockWebServer.enqueue(jsonResponse("{\"message\":\"Inventory does not exist\"}").setResponseCode(404));

        StepVerifier.create(inventoryServiceClient.getSupplyHistory("missing", "p1"))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(InventoryNotFoundException.class, error);
                    assertEquals("Inventory does not exist", error.getMessage());
                })
                .verify(TIMEOUT);
        takeRequest("GET", "/inventory/missing/products/p1/history");
    }

    @Test
    void paginatedSupplySearchForwardsEveryFilter() throws InterruptedException {
        mockWebServer.enqueue(jsonResponse("[]"));

        StepVerifier.create(inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductFieldPagination(
                        "inv1", "Gaze", 2.0, 10.0, 5, 3.0, 12.0, Optional.of(1), Optional.of(20)))
                .expectComplete().verify(TIMEOUT);

        RecordedRequest sent = mockWebServer.takeRequest(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        assertNotNull(sent, "Expected a request to the inventory service");
        assertEquals("GET", sent.getMethod());
        HttpUrl url = sent.getRequestUrl();
        assertNotNull(url);
        assertEquals("/inventory/inv1/products-pagination", url.encodedPath());
        assertEquals("1", url.queryParameter("page"));
        assertEquals("20", url.queryParameter("size"));
        assertEquals("Gaze", url.queryParameter("productName"));
        assertEquals("2.0", url.queryParameter("minPrice"));
        assertEquals("10.0", url.queryParameter("maxPrice"));
        assertEquals("5", url.queryParameter("productQuantity"));
        assertEquals("3.0", url.queryParameter("minSalePrice"));
        assertEquals("12.0", url.queryParameter("maxSalePrice"));
    }

    @Test
    void addingSupplyForwardsActor() throws InterruptedException {
        mockWebServer.enqueue(jsonResponse(PRODUCT_RESPONSE));
        StepVerifier.create(inventoryServiceClient.addSupplyToInventory(new ProductRequestDTO(), "inv1", "manager"))
                .assertNext(product -> assertEquals("p1", product.getProductId()))
                .expectComplete().verify(TIMEOUT);

        assertEquals("manager", takeRequest("POST", "/inventory/inv1/products").getHeader("X-Actor-Username"));
    }

    @Test
    void consumingSupplyForwardsActor() throws InterruptedException {
        mockWebServer.enqueue(jsonResponse(PRODUCT_RESPONSE));
        StepVerifier.create(inventoryServiceClient.consumeProduct("inv1", "p1", "manager"))
                .assertNext(product -> assertEquals("p1", product.getProductId()))
                .expectComplete().verify(TIMEOUT);

        assertEquals("manager", takeRequest("PATCH", "/inventory/inv1/products/p1/consume")
                .getHeader("X-Actor-Username"));
    }

    @Test
    void restockingSupplyForwardsQuantityAndActor() throws InterruptedException {
        mockWebServer.enqueue(jsonResponse(PRODUCT_RESPONSE));
        StepVerifier.create(inventoryServiceClient.restockLowStockProduct("inv1", "p1", 8, "manager"))
                .assertNext(product -> assertEquals("p1", product.getProductId()))
                .expectComplete().verify(TIMEOUT);

        assertEquals("manager", takeRequest("PUT", "/inventory/inv1/products/p1/restockProduct?productQuantity=8")
                .getHeader("X-Actor-Username"));
    }

    @Test
    void deletingSupplyForwardsActor() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        StepVerifier.create(inventoryServiceClient.deleteProductInInventory("inv1", "p1", "manager"))
                .expectComplete().verify(TIMEOUT);

        assertEquals("manager", takeRequest("DELETE", "/inventory/inv1/products/p1")
                .getHeader("X-Actor-Username"));
    }

    private MockResponse jsonResponse(String body) {
        return new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(body);
    }

    private RecordedRequest takeRequest(String method, String path) throws InterruptedException {
        RecordedRequest request = mockWebServer.takeRequest(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        assertNotNull(request, "Expected a request to the inventory service");
        assertEquals(method, request.getMethod());
        assertEquals(path, request.getPath());
        return request;
    }
}
