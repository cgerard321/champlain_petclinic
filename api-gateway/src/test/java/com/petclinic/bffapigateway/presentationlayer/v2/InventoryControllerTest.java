package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.InventoryServiceClient;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.exceptions.InventoryNotFoundException;
import com.petclinic.bffapigateway.presentationlayer.v1.InventoryControllerV1;
import com.petclinic.bffapigateway.utils.InventoryUtils.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        InventoryControllerV1.class,
        InventoryServiceClient.class
})
@WebFluxTest(controllers = InventoryControllerV1.class)
@AutoConfigureWebTestClient
public class InventoryControllerTest {
    @Autowired
    private WebTestClient client;
    @MockBean
    private InventoryServiceClient inventoryServiceClient;
    private final String baseInventoryURL = "/api/gateway/inventories";

    InputStream inputStream = getClass().getResourceAsStream("/images/DiagnosticKitImage.jpg");
    byte[] diagnosticKitImage = ImageUtil.readImage(inputStream);

    public InventoryControllerTest() throws IOException {
    }

    private InventoryResponseDTO buildInventoryDTO(){
        return InventoryResponseDTO.builder()
                .inventoryId("1")
                .inventoryName("invt1")
                .inventoryType("Internal")
                .inventoryDescription("invtone")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .imageUploaded(diagnosticKitImage)
                .build();
    }
    private List<InventoryTypeResponseDTO> buildInventoryTypeResponseDTOList(){
        return List.of(InventoryTypeResponseDTO.builder()
                .type("Internal")
                .typeId("1")
                .build(), InventoryTypeResponseDTO.builder().typeId("2").type("External").build());
    }

    @Test
    void deleteAllInventories_shouldSucceed() {
        // Arrange
        when(inventoryServiceClient.deleteAllInventories())
                .thenReturn(Mono.empty());  // Using Mono.empty() to simulate a void return (successful deletion without a return value).

        // Act
        client.delete()
                .uri("/api/gateway/inventories")  // Assuming the endpoint for deleting all inventories is the same without an ID.
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        // Assert
        verify(inventoryServiceClient, times(1))
                .deleteAllInventories();
    }


    @Test
    void getAllInventories_withValidPageSize_and_PageNumber_shouldReturnInventories() {
        //Arrange
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(2);
        when(inventoryServiceClient.searchInventory(page, size, null,null, null, null, null))
                .thenReturn(Flux.just(buildInventoryDTO()));

        // Act
        client.get()
                .uri(baseInventoryURL + "?page=0&size=2")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(1)
                .contains(buildInventoryDTO());

        // Assert
        verify(inventoryServiceClient, times(1))
                .searchInventory(eq(page), eq(size), eq(null), eq(null), eq(null), eq(null), eq(null));
    }

    @Test
    void getAllInventories_with_ValidQueryParams_InventoryName_InventoryType_InventoryDescription_shouldReturnOneInventory() {
        // Arrange
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(2);
        when(inventoryServiceClient.searchInventory(page, size, null, "invt1", "Internal", "invtone", null))
                .thenReturn(Flux.just(buildInventoryDTO()));

        // Act
        client.get()
                .uri(baseInventoryURL + "?page=0&size=2&inventoryName=invt1&inventoryType=Internal&inventoryDescription=invtone")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(1)
                .contains(buildInventoryDTO());

        // Assert
        verify(inventoryServiceClient, times(1))
                .searchInventory(eq(page), eq(size), eq(null), eq("invt1"), eq("Internal"), eq("invtone"), eq(null));
    }

    @Test
    void getAllInventories_with_Invalid_QueryParams_shouldReturnEmptyList() {
        // Arrange
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(2);
        when(inventoryServiceClient.searchInventory(page, size, null, "invalid", "invalid", "invalid", null))
                .thenReturn(Flux.empty());

        // Act
        client.get()
                .uri(baseInventoryURL + "?page=0&size=2&inventoryName=invalid&inventoryType=invalid&inventoryDescription=invalid")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(0);

        // Assert
        verify(inventoryServiceClient, times(1))
                .searchInventory(eq(page), eq(size), eq(null), eq("invalid"), eq("invalid"), eq("invalid"), eq(null));
    }

    @Test
    void getAllInventoryTypes_shouldReturnInventoryTypes() {
        // Arrange
        when(inventoryServiceClient.getAllInventoryTypes())
                .thenReturn(Flux.fromIterable(buildInventoryTypeResponseDTOList()));

        // Act
        client.get()
                .uri(baseInventoryURL + "/types")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryTypeResponseDTO.class)
                .hasSize(2);

        // Assert
        verify(inventoryServiceClient, times(1))
                .getAllInventoryTypes();
    }

    @Test
    void deleteInventoryById_WithValidInventoryId_ShouldSucceed() {
        // Arrange
        String inventoryId = "1";
        when(inventoryServiceClient.deleteInventoryByInventoryId(inventoryId))
                .thenReturn(Mono.empty());

        // Act
        client.delete()
                .uri(baseInventoryURL + "/" + inventoryId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        // Assert
        verify(inventoryServiceClient, times(1))
                .deleteInventoryByInventoryId(eq(inventoryId));
    }

    @Test
    void deleteInventoryById_WithInvalid_ShouldReturnNotFound() {
        // Arrange
        String inventoryId = "invalid";
        when(inventoryServiceClient.deleteInventoryByInventoryId(inventoryId))
                .thenThrow(new NotFoundException("Inventory not found"));

        // Act
        client.delete()
                .uri(baseInventoryURL + "/" + inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        // Assert
        verify(inventoryServiceClient, times(1))
                .deleteInventoryByInventoryId(inventoryId);
    }

    @Test
    void getInventoryById_withValidId_shouldReturnInventory() {
        // Arrange
        String inventoryId = "1";
        InventoryResponseDTO inventory = buildInventoryDTO();
        when(inventoryServiceClient.getInventoryById(inventoryId))
                .thenReturn(Mono.just(inventory));

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryResponseDTO.class)
                .isEqualTo(inventory);

        // Assert
        verify(inventoryServiceClient, times(1))
                .getInventoryById(eq(inventoryId));
    }

    @Test
    void getInventoryById_withInvalidIdFormat_shouldReturnBadRequest() {
        // Arrange
        String invalidInventoryId = "invalid-id-format";
        when(inventoryServiceClient.getInventoryById(invalidInventoryId))
                .thenReturn(Mono.empty());

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + invalidInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        // Assert
        verify(inventoryServiceClient, times(1))
                .getInventoryById(eq(invalidInventoryId));
    }

    @Test
    void getInventoryById_withNonExistentId_shouldReturnNotFound() {
        // Arrange
        String nonExistentInventoryId = "non-existent-id";
        when(inventoryServiceClient.getInventoryById(nonExistentInventoryId))
                .thenReturn(Mono.empty());

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + nonExistentInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        // Assert
        verify(inventoryServiceClient, times(1))
                .getInventoryById(eq(nonExistentInventoryId));
    }

    @Test
    void updateInventoryById_withValidId_shouldReturnUpdatedInventory() {
        // Arrange
        String inventoryId = "dfa0a7e3-5a40-4b86-881e-9549ecda5e4b";
        InventoryRequestDTO updateRequest = InventoryRequestDTO.builder()
                .inventoryName("updatedName")
                .inventoryType("updatedType")
                .inventoryDescription("updatedDescription")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .imageUploaded(diagnosticKitImage)
                .build();
        InventoryResponseDTO updatedInventory = InventoryResponseDTO.builder()
                .inventoryId(inventoryId)
                .inventoryName("updatedName")
                .inventoryType("updatedType")
                .inventoryDescription("updatedDescription")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .imageUploaded(diagnosticKitImage)
                .build();

        when(inventoryServiceClient.updateInventory(eq(updateRequest), eq(inventoryId)))
                .thenReturn(Mono.just(updatedInventory));

        // Act
        client.put()
                .uri(baseInventoryURL + "/" + inventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryResponseDTO.class)
                .isEqualTo(updatedInventory);

        // Assert
        verify(inventoryServiceClient, times(1))
                .updateInventory(eq(updateRequest), eq(inventoryId));
    }

    @Test
    void updateInventoryById_withServiceLayerFailure_shouldReturnInternalServerError() {
        // Arrange
        String validInventoryId = "dfa0a7e3-5a40-4b86-881e-9549ecda5e4b";
        InventoryRequestDTO updateRequest = InventoryRequestDTO.builder()
                .inventoryName("updatedName")
                .inventoryType("updatedType")
                .inventoryDescription("updatedDescription")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .imageUploaded(diagnosticKitImage)
                .build();

        when(inventoryServiceClient.updateInventory(eq(updateRequest), eq(validInventoryId)))
                .thenReturn(Mono.error(new RuntimeException("Service failure")));  // Simulating service failure.

        // Act
        client.put()
                .uri(baseInventoryURL + "/" + validInventoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().is5xxServerError();  // Expecting 500 Internal Server Error.

        // Assert
        verify(inventoryServiceClient, times(1))
                .updateInventory(eq(updateRequest), eq(validInventoryId));
    }

    @Test
    void addInventory_withValidInventoryRequest_shouldReturnCreatedInventory() {
        // Arrange
        InventoryRequestDTO inventoryRequest = InventoryRequestDTO.builder()
                .inventoryName("invt1")
                .inventoryType("Internal")
                .inventoryDescription("invtone")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .imageUploaded(diagnosticKitImage)
                .build();
        InventoryResponseDTO createdInventory = buildInventoryDTO();

        when(inventoryServiceClient.addInventory(eq(inventoryRequest)))
                .thenReturn(Mono.just(createdInventory));

        // Act
        client.post()
                .uri(baseInventoryURL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(inventoryRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(InventoryResponseDTO.class)
                .isEqualTo(createdInventory);

        // Assert
        verify(inventoryServiceClient, times(1))
                .addInventory(eq(inventoryRequest));
    }

    @Test
    void addInventory_withInvalidInventoryRequest_shouldReturnBadRequest() {
        // Arrange
        InventoryRequestDTO invalidInventoryRequest = InventoryRequestDTO.builder()
                .inventoryName("invt1")
                .inventoryType("Internal")
                .inventoryDescription("invtone")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .imageUploaded(diagnosticKitImage)
                .build();

        when(inventoryServiceClient.addInventory(eq(invalidInventoryRequest)))
                .thenReturn(Mono.empty());

        // Act
        client.post()
                .uri(baseInventoryURL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidInventoryRequest)
                .exchange()
                .expectStatus().isBadRequest();

        // Assert
        verify(inventoryServiceClient, times(1))
                .addInventory(eq(invalidInventoryRequest));
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus_withValidInventoryIdAndProductNameAndProductDescriptionAndStatus_shouldReturnProducts() {
        // Arrange
        String inventoryId = "1";
        String productName = "product1";
        String productDescription = "productone";
        Status status = Status.AVAILABLE;
        ProductResponseDTO product = ProductResponseDTO.builder()
                .productId("1")
                .productName("product1")
                .productDescription("productone")
                .status(Status.AVAILABLE)
                .build();

        when(inventoryServiceClient.searchProducts(inventoryId, productName, productDescription, status))
                .thenReturn(Flux.just(product));

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/search?productName=product1&productDescription=productone&status=AVAILABLE")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(1)
                .contains(product);

        // Assert
        verify(inventoryServiceClient, times(1))
                .searchProducts(eq(inventoryId), eq(productName), eq(productDescription), eq(status));
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescriptionAndStatus_withInvalidInventoryId_shouldReturnNoContent() {
        // Arrange
        String invalidInventoryId = "invalid";
        String productName = "product1";
        String productDescription = "productone";
        Status status = Status.AVAILABLE;

        when(inventoryServiceClient.searchProducts(invalidInventoryId, productName, productDescription, status))
                .thenReturn(Flux.empty());

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + invalidInventoryId + "/products/search?productName=product1&productDescription=productone&status=AVAILABLE")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        // Assert
        verify(inventoryServiceClient, times(1))
                .searchProducts(eq(invalidInventoryId), eq(productName), eq(productDescription), eq(status));
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescription_withValidStatusAndInvalidProductNameAndProductDescription_shouldReturnEmptyList() {
        // Arrange
        String inventoryId = "1";
        String invalidProductName = "invalid";
        String invalidProductDescription = "invalid";
        Status status = Status.AVAILABLE;

        when(inventoryServiceClient.searchProducts(inventoryId, invalidProductName, invalidProductDescription, status))
                .thenReturn(Flux.empty());

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/search?productName=invalid&productDescription=invalid&status=AVAILABLE")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        // Assert
        verify(inventoryServiceClient, times(1))
                .searchProducts(eq(inventoryId), eq(invalidProductName), eq(invalidProductDescription), eq(status));
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescription_withValidInventoryIdAndInvalidProductNameAndProductDescription_shouldReturnEmptyList() {
        // Arrange
        String inventoryId = "1";
        String invalidProductName = "invalid";
        String invalidProductDescription = "invalid";

        when(inventoryServiceClient.searchProducts(inventoryId, invalidProductName, invalidProductDescription, null))
                .thenReturn(Flux.empty());

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/search?productName=invalid&productDescription=invalid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        // Assert
        verify(inventoryServiceClient, times(1))
                .searchProducts(eq(inventoryId), eq(invalidProductName), eq(invalidProductDescription), eq(null));
    }

    @Test
    void searchProductsByInventoryIdAndProductNameAndProductDescription_withValidInventoryIdAndProductNameAndProductDescriptionAndStatus_shouldReturnProducts() {
        // Arrange
        String inventoryId = "1";
        String productName = "product1";
        String productDescription = "productone";
        Status status = Status.AVAILABLE;
        ProductResponseDTO product = ProductResponseDTO.builder()
                .productId("1")
                .productName("product1")
                .productDescription("productone")
                .productPrice(100.0)
                .status(Status.AVAILABLE)
                .build();

        when(inventoryServiceClient.searchProducts(inventoryId, productName, productDescription, status))
                .thenReturn(Flux.just(product));

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/search?productName=product1&productDescription=productone&status=AVAILABLE")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(1)
                .contains(product);
        // Assert
        verify(inventoryServiceClient, times(1))
                .searchProducts(eq(inventoryId), eq(productName), eq(productDescription), eq(status));
    }

    @Test
    void getProductByProductIdInInventory_withValidInventoryIdAndProductId_shouldReturnProduct() {
        // Arrange
        String inventoryId = "1";
        String productId = "101";
        ProductResponseDTO productResponseDTO = ProductResponseDTO.builder()
                .productId(productId)
                .productName("Product 101")
                .productDescription("Description of Product 101")
                .productPrice(99.99)
                .build();

        when(inventoryServiceClient.getProductByProductIdInInventory(inventoryId, productId))
                .thenReturn(Mono.just(productResponseDTO));

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(productResponseDTO);

        // Assert
        verify(inventoryServiceClient, times(1))
                .getProductByProductIdInInventory(inventoryId, productId);
    }

    @Test
    void getProductByProductIdInInventory_withInvalidProductId_shouldReturnNotFound() {
        // Arrange
        String inventoryId = "1";
        String invalidProductId = "999";
        when(inventoryServiceClient.getProductByProductIdInInventory(inventoryId, invalidProductId))
                .thenReturn(Mono.empty());

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/" + invalidProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        // Assert
        verify(inventoryServiceClient, times(1))
                .getProductByProductIdInInventory(inventoryId, invalidProductId);
    }

    @Test
    void updateProductInInventory_withValidData_shouldReturnUpdatedProduct() {
        // Arrange
        String inventoryId = "1";
        String productId = "101";
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Updated Product 101")
                .productDescription("Updated Description")
                .productPrice(149.99)
                .build();

        ProductResponseDTO updatedProductResponseDTO = ProductResponseDTO.builder()
                .productId(productId)
                .productName("Updated Product 101")
                .productDescription("Updated Description")
                .productPrice(149.99)
                .build();

        when(inventoryServiceClient.updateProductInInventory(productRequestDTO, inventoryId, productId))
                .thenReturn(Mono.just(updatedProductResponseDTO));

        // Act
        client.put()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(updatedProductResponseDTO);

        // Assert
        verify(inventoryServiceClient, times(1))
                .updateProductInInventory(productRequestDTO, inventoryId, productId);
    }

    @Test
    void updateProductInInventory_withInvalidData_shouldReturnBadRequest() {
        // Arrange
        String inventoryId = "1";
        String productId = "101";
        ProductRequestDTO invalidProductRequestDTO = ProductRequestDTO.builder()
                .productName("")
                .productDescription("")
                .productPrice(-10.0)
                .build();

        when(inventoryServiceClient.updateProductInInventory(invalidProductRequestDTO, inventoryId, productId))
                .thenReturn(Mono.empty());

        // Act
        client.put()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidProductRequestDTO)
                .exchange()
                .expectStatus().isBadRequest();

        // Assert
        verify(inventoryServiceClient, times(1))
                .updateProductInInventory(invalidProductRequestDTO, inventoryId, productId);
    }

    @Test
    void getQuantityOfProductsInInventory_withValidInventoryId_shouldReturnProductQuantity() {
        // Arrange
        String inventoryId = "1";
        Integer expectedQuantity = 100;  // Simulating that there are 100 products in the inventory

        // Mocking the service to return the expected quantity
        when(inventoryServiceClient.getQuantityOfProductsInInventory(inventoryId))
                .thenReturn(Mono.just(expectedQuantity));

        // Act and Assert
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/productquantity")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .value(quantity -> {
                    assertNotNull(quantity);
                    assertEquals(expectedQuantity, quantity);
                });

        // Verify that the service was called with the correct inventoryId
        verify(inventoryServiceClient, times(1))
                .getQuantityOfProductsInInventory(eq(inventoryId));
    }

    @Test
    void getQuantityOfProductsInInventory_withServerError_shouldReturnInternalServerError() {
        // Arrange
        String inventoryId = "1";
        String errorMessage = "Internal Server Error";

        // Mocking the service to throw an error (simulating a 500 error from the server)
        when(inventoryServiceClient.getQuantityOfProductsInInventory(inventoryId))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        // Act and Assert
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/productquantity")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Internal Server Error");

        // Verify that the service was called with the correct inventoryId
        verify(inventoryServiceClient, times(1))
                .getQuantityOfProductsInInventory(eq(inventoryId));
    }

    @Test
    void addSupplyToInventory_withInvalidRequest_shouldReturnBadRequest() {
        // Arrange
        ProductRequestDTO invalidProductRequestDTO = ProductRequestDTO.builder()
                .productName("")
                .productDescription("")
                .productPrice(-10.0)
                .build();

        String inventoryId = "1"; // Define the inventoryId

        when(inventoryServiceClient.addSupplyToInventory(invalidProductRequestDTO, inventoryId))
                .thenReturn(Mono.empty());

        // Act
        client.post()
                .uri(baseInventoryURL + "/" + inventoryId + "/products") // Use the inventoryId variable
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidProductRequestDTO)
                .exchange()
                .expectStatus().isBadRequest();

        // Assert
        verify(inventoryServiceClient, times(1))
                .addSupplyToInventory(invalidProductRequestDTO, inventoryId);
    }

    @Test
    void addSupplyToInventory_withValidRequest_shouldReturnCreatedProduct() {
        // Arrange
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Product 101")
                .productDescription("Description of Product 101")
                .productPrice(99.99)
                .build();

        String inventoryId = "1"; // Define the inventoryId

        ProductResponseDTO createdProductResponseDTO = ProductResponseDTO.builder()
                .productId("101")
                .productName("Product 101")
                .productDescription("Description of Product 101")
                .productPrice(99.99)
                .build();

        when(inventoryServiceClient.addSupplyToInventory(productRequestDTO, inventoryId))
                .thenReturn(Mono.just(createdProductResponseDTO));

        // Act
        client.post()
                .uri(baseInventoryURL + "/" + inventoryId + "/products") // Use the inventoryId variable
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(createdProductResponseDTO);

        // Assert
        verify(inventoryServiceClient, times(1))
                .addSupplyToInventory(productRequestDTO, inventoryId);
    }


    @Test
    void consumeProduct_withValidData_shouldReturnUpdatedProduct() {
        // Arrange
        String inventoryId = "1";
        String productId = "101";
        ProductRequestDTO productRequestDTO = ProductRequestDTO.builder()
                .productName("Product 101")
                .productDescription("Description of Product 101")
                .productPrice(99.99)
                .build();

        ProductResponseDTO updatedProductResponseDTO = ProductResponseDTO.builder()
                .productId(productId)
                .productName("Product 101")
                .productDescription("Description of Product 101")
                .productPrice(99.99)
                .build();

        when(inventoryServiceClient.consumeProduct(inventoryId, productId))
                .thenReturn(Mono.just(updatedProductResponseDTO));

        // Act
        client.patch()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/" + productId + "/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(updatedProductResponseDTO);

        // Assert
        verify(inventoryServiceClient, times(1))
                .consumeProduct(inventoryId, productId);
    }

    @Test
    void consumeProduct_withInvalidData_shouldReturnNotFound() {
        // Arrange
        String inventoryId = "1";
        String invalidProductId = "999";
        when(inventoryServiceClient.consumeProduct(inventoryId, invalidProductId))
                .thenReturn(Mono.empty());

        // Act
        client.patch()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/" + invalidProductId + "/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        // Assert
        verify(inventoryServiceClient, times(1))
                .consumeProduct(inventoryId, invalidProductId);
    }

    @Test
    void deleteAllProductsInInventory_ShouldSucceed() {

        String inventoryId = "inventory1";
        // Mock the service call to simulate the successful deletion of all products in an inventory.
        // Assuming your service client has a method called `deleteAllProductsInInventory`.
        when(inventoryServiceClient.deleteAllProductsInInventory(inventoryId))
                .thenReturn(Mono.empty());  // Using Mono.empty() to simulate a void return (successful deletion without a return value).

        // Make the DELETE request to the API.
        client.delete()
                .uri("/api/gateway/inventories/" + inventoryId +"/products")  // Assuming the endpoint for deleting all products in an inventory is the same with the inventory ID.
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        // Verify that the deleteAllProductsInInventory method on the service client was called exactly once.
        verify(inventoryServiceClient, times(1))
                .deleteAllProductsInInventory(inventoryId);
    }

    @Test
    public void testCreateSupplyPdf_NotFound() {
        client.get()
                .uri("/inventory/validInventoryId/products/download")
                .exchange()
                .expectStatus().isNotFound();

        // Verify the service method was not called
        verify(inventoryServiceClient, never()).createSupplyPdf(anyString());
    }
    @Test
    public void testCreateSupplyPdf_Success() {
        // Arrange
        String inventoryId = "inventory1";
        byte[] pdfContent = "PDF Content".getBytes();

        // Mock the service method to return the PDF content
        when(inventoryServiceClient.createSupplyPdf(inventoryId))
                .thenReturn(Mono.just(pdfContent));

        // Act
        client.get()
                .uri(baseInventoryURL + "/" + inventoryId + "/products/download")
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .isEqualTo(pdfContent);

        // Verify that the service method was called with the correct inventoryId
        verify(inventoryServiceClient, times(1))
                .createSupplyPdf(eq(inventoryId));
    }

    @Test
    void updateProductInventoryId_withValidData_shouldReturnUpdatedProduct() {
        // Arrange
        String currentInventoryId = "1";
        String productId = "101";
        String newInventoryId = "2";
        ProductResponseDTO updatedProductResponseDTO = ProductResponseDTO.builder()
                .productId(productId)
                .productName("Updated Product 101")
                .productDescription("Updated Description")
                .productPrice(149.99)
                .build();

        when(inventoryServiceClient.updateProductInventoryId(currentInventoryId, productId, newInventoryId))
                .thenReturn(Mono.just(updatedProductResponseDTO));

        // Act
        client.put()
                .uri(baseInventoryURL + "/" + currentInventoryId + "/products/" + productId + "/updateInventoryId/" + newInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(updatedProductResponseDTO);

        // Assert
        verify(inventoryServiceClient, times(1))
                .updateProductInventoryId(currentInventoryId, productId, newInventoryId);
    }

    @Test
    void updateProductInventoryId_withInvalidData_shouldReturnNotFound() {
        // Arrange
        String currentInventoryId = "1";
        String productId = "999"; // Assuming this product doesn't exist
        String newInventoryId = "2";
        when(inventoryServiceClient.updateProductInventoryId(currentInventoryId, productId, newInventoryId))
                .thenReturn(Mono.empty());

        // Act
        client.put()
                .uri(baseInventoryURL + "/" + currentInventoryId + "/products/" + productId + "/updateInventoryId/" + newInventoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        // Assert
        verify(inventoryServiceClient, times(1))
                .updateProductInventoryId(currentInventoryId, productId, newInventoryId);
    }

    @Test
    void getAllInventories_shouldReturnAllInventories() {
        // Arrange
        InventoryResponseDTO inventoryResponseDTO1 = buildInventoryDTO();
        InventoryResponseDTO inventoryResponseDTO2 = buildInventoryDTO().builder()
                .inventoryId("2")
                .inventoryName("invt2")
                .build();

        when(inventoryServiceClient.getAllInventories())
                .thenReturn(Flux.just(inventoryResponseDTO1, inventoryResponseDTO2));

        // Act
        client.get()
                .uri(baseInventoryURL + "/all")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(2)
                .contains(inventoryResponseDTO1, inventoryResponseDTO2);

        // Assert
        verify(inventoryServiceClient, times(1)).getAllInventories();
    }

    @Test
    void restockLowStockProduct_withValidRequest_ShouldSucceed() {
        // Arrange
        String inventoryId = "inventory1";
        String productId = "product1";
        Integer productQuantity = 10;

        ProductResponseDTO restockedProduct = ProductResponseDTO.builder()
                .productId(productId)
                .inventoryId(inventoryId)
                .productName("Restocked Product")
                .productDescription("Restocked Product Description")
                .productPrice(100.0)
                .productQuantity(productQuantity)
                .build();

        when(inventoryServiceClient.restockLowStockProduct(inventoryId, productId, productQuantity))
                .thenReturn(Mono.just(restockedProduct));

        // Act and Assert
        client.put()
                .uri("/api/gateway/inventories/" + inventoryId + "/products/" + productId + "/restockProduct?productQuantity=" + productQuantity)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(restockedProduct);

        // Verify that the service client was called
        verify(inventoryServiceClient, times(1))
                .restockLowStockProduct(inventoryId, productId, productQuantity);
    }


    @Test
    void restockLowStockProduct_withInvalidQuantity_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "inventory1";
        String productId = "product1";
        Integer invalidQuantity = -5;

        // Act and Assert
        client.put()
                .uri("/api/gateway/inventories/" + inventoryId + "/products/" + productId + "/restockProduct?productQuantity=" + invalidQuantity)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify that the service client was not called
        verify(inventoryServiceClient, never())
                .restockLowStockProduct(inventoryId, productId, invalidQuantity);
    }

    @Test
    void restockLowStockProduct_withNullProductQuantity_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "inventory1";
        String productId = "product1";
        Integer productQuantity = null; // Simulating a null quantity

        // Act and Assert
        client.put()
                .uri("/api/gateway/inventories/" + inventoryId + "/products/" + productId + "/restockProduct")
                .exchange()
                .expectStatus().isBadRequest(); // Expecting 400 Bad Request

        // Verify that the service client was never called
        verify(inventoryServiceClient, never())
                .restockLowStockProduct(eq(inventoryId), eq(productId), eq(productQuantity));
    }

    @Test
    void restockLowStockProduct_withNegativeProductQuantity_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "inventory1";
        String productId = "product1";
        Integer productQuantity = -5; // Simulating a negative quantity

        // Act and Assert
        client.put()
                .uri("/api/gateway/inventories/" + inventoryId + "/products/" + productId + "/restockProduct?productQuantity=" + productQuantity)
                .exchange()
                .expectStatus().isBadRequest(); // Expecting 400 Bad Request

        // Verify that the service client was never called
        verify(inventoryServiceClient, never())
                .restockLowStockProduct(eq(inventoryId), eq(productId), eq(productQuantity));
    }

    @Test
    void restockLowStockProduct_withZeroProductQuantity_ShouldReturnBadRequest() {
        // Arrange
        String inventoryId = "inventory1";
        String productId = "product1";
        Integer productQuantity = 0; // Simulating zero quantity

        // Act and Assert
        client.put()
                .uri("/api/gateway/inventories/" + inventoryId + "/products/" + productId + "/restockProduct?productQuantity=" + productQuantity)
                .exchange()
                .expectStatus().isBadRequest(); // Expecting 400 Bad Request

        // Verify that the service client was never called
        verify(inventoryServiceClient, never())
                .restockLowStockProduct(eq(inventoryId), eq(productId), eq(productQuantity));
    }

    @Test
    void getProductsInInventoryByInventoryIdAndProductFieldPagination(){
        ProductResponseDTO expectedResponse = ProductResponseDTO.builder()
                .productId("1234")
                .inventoryId("1")
                .productName("testName")
                .productDescription("testDescription")
                .productPrice(65.00)
                .productQuantity(3)
                .build();
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(2);
        Flux<ProductResponseDTO> resp = Flux.just(expectedResponse);
        when(inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductFieldPagination("1", null, null, null, null, null, null, page, size))
                .thenReturn(resp);
        client.get()
                .uri("/api/gateway/inventories/{inventoryId}/products-pagination?page={page}&size={size}","1", page.get(), size.get())
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange().expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type","text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseDTO.class)
                .value((list)-> {
                    assertEquals(1,list.size());
                    assertEquals(list.get(0).getProductId(),expectedResponse.getProductId());
                    assertEquals(list.get(0).getInventoryId(),expectedResponse.getInventoryId());
                    assertEquals(list.get(0).getProductName(),expectedResponse.getProductName());
                    assertEquals(list.get(0).getProductDescription(),expectedResponse.getProductDescription());
                    assertEquals(list.get(0).getProductPrice(),expectedResponse.getProductPrice());
                    assertEquals(list.get(0).getProductQuantity(),expectedResponse.getProductQuantity());
                });
    }

    @Test
    void getProductsInInventory_withOnlyMinPrice_shouldSucceed() {
        when(inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductsField(
                "1", null, 50.00, null, null, null, null))
                .thenReturn(Flux.just(buildProductDTO()));

        client.get()
                .uri("/api/gateway/inventories/1/products?minPrice=50.00")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getProductsInInventory_withOnlyMaxPrice_shouldSucceed() {
        when(inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductsField(
                "1", null, null, 200.00, null, null, null))
                .thenReturn(Flux.just(buildProductDTO()));

        client.get()
                .uri("/api/gateway/inventories/1/products?maxPrice=200.00")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getTotalNumberOfProductsWithRequestParams(){
        ProductResponseDTO expectedResponse = ProductResponseDTO.builder()
                .productId("1234")
                .inventoryId("1")
                .productName("testName")
                .productDescription("testDescription")
                .productPrice(65.00)
                .productQuantity(3)
                .build();
        when(inventoryServiceClient.getTotalNumberOfProductsWithRequestParams("1", null, null, null, null, null, null))
                .thenReturn(Flux.just(expectedResponse).count());
        client.get()
                .uri("/api/gateway/inventories/{inventoryId}/products-count","1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Long.class)
                .value((count)-> {
                    assertEquals(1L,count.longValue());
                });
    }

    @Test
    void testUpdateProductInInventory() {
        // Create a sample ProductRequestDTO
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100, 15.99, null);

        // Define the expected response
        ProductResponseDTO expectedResponse = ProductResponseDTO.builder()
                .productId("sampleProductId")
                .inventoryId("sampleInventoryId")
                .productName(requestDTO.getProductName())
                .productDescription(requestDTO.getProductDescription())
                .productPrice(requestDTO.getProductPrice())
                .productQuantity(requestDTO.getProductQuantity())
                .build();

        // Mock the behavior of the inventoryServiceClient
        when(inventoryServiceClient.updateProductInInventory(any(), anyString(), anyString()))
                .thenReturn(Mono.just(expectedResponse));

        // Perform the PUT request
        client.put()
                .uri("/api/gateway/inventories/{inventoryId}/products/{productId}", "sampleInventoryId", "sampleProductId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseDTO.class)
                .value(dto ->{
                    assertNotNull(dto);
                    assertEquals(requestDTO.getProductName(),dto.getProductName());
                    assertEquals(requestDTO.getProductDescription(),dto.getProductDescription());
                    assertEquals(requestDTO.getProductPrice(),dto.getProductPrice());
                    assertEquals(requestDTO.getProductQuantity(),dto.getProductQuantity());
                });

        // Verify that the inventoryServiceClient method was called
        verify(inventoryServiceClient, times(1))
                .updateProductInInventory(eq(requestDTO), eq("sampleInventoryId"), eq("sampleProductId"));
    }

    @Test
    @DisplayName("Given valid inventoryId and valid productRequest Post and return productResponse")
    void testAddProductToInventory_ShouldSucceed() {
        // Create a sample ProductRequestDTO
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100, 15.99, null);

        // Define the expected response
        ProductResponseDTO expectedResponse = ProductResponseDTO.builder()
                .productId("sampleProductId")
                .inventoryId("sampleInventoryId")
                .productName(requestDTO.getProductName())
                .productDescription(requestDTO.getProductDescription())
                .productPrice(requestDTO.getProductPrice())
                .productQuantity(requestDTO.getProductQuantity())
                .productSalePrice(requestDTO.getProductSalePrice())
                .build();

        // Mock the behavior of the inventoryServiceClient
        when(inventoryServiceClient.addSupplyToInventory(any(), anyString()))
                .thenReturn(Mono.just(expectedResponse));

        // Perform the POST request
        client.post()
                .uri("/api/gateway/inventories/{inventoryId}/products", "sampleInventoryId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(requestDTO.getProductName(), dto.getProductName());
                    assertEquals(requestDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(requestDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(requestDTO.getProductQuantity(), dto.getProductQuantity());
                });

        // Verify that the inventoryServiceClient method was called
        verify(inventoryServiceClient, times(1))
                .addSupplyToInventory(eq(requestDTO), eq("sampleInventoryId"));
    }

    @Test
    @DisplayName("Given invalid inventoryId and valid productRequest Post and return NotFoundException")
    void testAddProductToInventory_InvalidInventoryId_ShouldReturnNotFoundException() {
        // Create a sample ProductRequestDTO
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100,15.99, null);


        // Mock the behavior of the inventoryServiceClient
        when(inventoryServiceClient.addSupplyToInventory(any(), anyString()))
                .thenReturn(Mono.error(new NotFoundException("Inventory not found")));

        // Perform the POST request
        client.post()
                .uri("/api/gateway/inventories/{inventoryId}/products", "invalidInventoryId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();

        // Verify that the inventoryServiceClient method was called
        verify(inventoryServiceClient, times(1))
                .addSupplyToInventory(eq(requestDTO), eq("invalidInventoryId"));
    }


    @Test
    void addInventory_withValidValue_shouldSucceed() {

        //InventoryRequestDTO requestDTO = new InventoryRequestDTO("internal", "Internal", "invt1");
        InventoryRequestDTO requestDTO = new InventoryRequestDTO();
        requestDTO.setInventoryName("invt1");
        requestDTO.setInventoryType("Internal");
        requestDTO.setInventoryDescription("newDescription");
        requestDTO.setInventoryImage("https://www.fda.gov/files/iStock-157317886.jpg");
        requestDTO.setInventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14");


        InventoryResponseDTO inventoryResponseDTO = buildInventoryDTO();

        when(inventoryServiceClient.addInventory(any()))
                .thenReturn(Mono.just(inventoryResponseDTO));

        client.post()
                .uri("/api/gateway/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.inventoryId").isEqualTo(inventoryResponseDTO.getInventoryId())
                .jsonPath("$.inventoryName").isEqualTo(inventoryResponseDTO.getInventoryName())
                .jsonPath("$.inventoryType").isEqualTo("Internal")
                .jsonPath("$.inventoryDescription").isEqualTo("invtone");


        verify(inventoryServiceClient, times(1))
                .addInventory(any());
    }



    @Test
    void updateInventory_withValidValue_shouldSucceed() {
        //InventoryRequestDTO requestDTO = new InventoryRequestDTO("internal", "Internal", "newDescription");
        String validInventoryId = "123e4567-e89b-12d3-a456-426614174000";

        InventoryRequestDTO requestDTO = new InventoryRequestDTO();
        requestDTO.setInventoryName("invt1");
        requestDTO.setInventoryType("Internal");
        requestDTO.setInventoryDescription("newDescription");
        requestDTO.setInventoryImage("https://www.fda.gov/files/iStock-157317886.jpg");
        requestDTO.setInventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14");


        InventoryResponseDTO expectedResponse = InventoryResponseDTO.builder()
                .inventoryId(validInventoryId)
                .inventoryName("newName")
                .inventoryType("Internal")
                .inventoryDescription("newDescription")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .build();

        when(inventoryServiceClient.updateInventory(any(), eq(validInventoryId)))
                .thenReturn(Mono.just(expectedResponse));


        client.put()
                .uri("/api/gateway/inventories/{inventoryId}", validInventoryId) // Use the appropriate URI
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.inventoryId").isEqualTo(expectedResponse.getInventoryId())
                .jsonPath("$.inventoryName").isEqualTo(expectedResponse.getInventoryName())
                .jsonPath("$.inventoryType").isEqualTo("Internal")
                .jsonPath("$.inventoryDescription").isEqualTo(expectedResponse.getInventoryDescription());

        verify(inventoryServiceClient, times(1))
                .updateInventory(any(), eq(validInventoryId));
    }

    private ProductResponseDTO buildProductDTO(){
        return ProductResponseDTO.builder()
                .inventoryId("1")
                .productId(UUID.randomUUID().toString())
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .productSalePrice(15.99)
                .build();
    }


    @Test
    void GetProductByInventoryIdAndProductId_InsideInventory() {
        ProductResponseDTO productResponseDTO = buildProductDTO();
        when(inventoryServiceClient.getProductByProductIdInInventory(productResponseDTO.getInventoryId(), productResponseDTO.getProductId()))
                .thenReturn(Mono.just(productResponseDTO));

        client.get()
                .uri("/api/gateway/inventories/{inventoryId}/products/{productId}", productResponseDTO.getInventoryId(), productResponseDTO.getProductId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(productResponseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(productResponseDTO.getProductId(), dto.getProductId());
                    assertEquals(productResponseDTO.getProductName(), dto.getProductName());
                    assertEquals(productResponseDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(productResponseDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(productResponseDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(productResponseDTO.getProductSalePrice(), dto.getProductSalePrice());

                });

        verify(inventoryServiceClient, times(1))
                .getProductByProductIdInInventory(productResponseDTO.getInventoryId(), productResponseDTO.getProductId());
    }


    @Test
    void getInventoryByInventoryId_ValidId_shouldSucceed() {
        String validInventoryId = "inventoryId_1";
        InventoryResponseDTO inventoryResponseDTO = InventoryResponseDTO.builder()
                .inventoryId(validInventoryId)
                .inventoryName("Pet food")
                .inventoryType("Internal")
                .inventoryDescription("pet")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .build();

        when(inventoryServiceClient.getInventoryById(validInventoryId))
                .thenReturn(Mono.just(inventoryResponseDTO));


        client.get()
                .uri("/api/gateway/inventories/{inventoryId}", validInventoryId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(InventoryResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(inventoryResponseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(inventoryResponseDTO.getInventoryName(), dto.getInventoryName());
                    assertEquals(inventoryResponseDTO.getInventoryType(), dto.getInventoryType());
                    assertEquals(inventoryResponseDTO.getInventoryDescription(), dto.getInventoryDescription());
                });


        verify(inventoryServiceClient, times(1))
                .getInventoryById(validInventoryId);
    }


    //delete all product inventory and delete all inventory
    @Test
    void deleteAllInventory_shouldSucceed() {
        // Mock the service call to simulate the successful deletion of all inventories.
        // Assuming your service client has a method called `deleteAllInventories`.
        when(inventoryServiceClient.deleteAllInventories())
                .thenReturn(Mono.empty());  // Using Mono.empty() to simulate a void return (successful deletion without a return value).

        // Make the DELETE request to the API.
        client.delete()
                .uri("/api/gateway/inventories")  // Assuming the endpoint for deleting all inventories is the same without an ID.
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        // Verify that the deleteAllInventories method on the service client was called exactly once.
        verify(inventoryServiceClient, times(1))
                .deleteAllInventories();
    }

    @Test
    public void deleteProductById_insideInventory(){
        ProductResponseDTO productResponseDTO = buildProductDTO();
        when(inventoryServiceClient.deleteProductInInventory(productResponseDTO.getInventoryId(), productResponseDTO.getProductId()))
                .thenReturn((Mono.empty()));

        client.delete()
                .uri("/api/gateway/inventories/{inventoryId}/products/{productId}",productResponseDTO.getInventoryId()  ,productResponseDTO.getProductId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(inventoryServiceClient, times(1))
                .deleteProductInInventory(productResponseDTO.getInventoryId(), productResponseDTO.getProductId());

    }

    @Test
    void getProductsByInventoryName_withProducts_shouldReturnsOk(){
        String inventoryName = "invt1";

        ProductResponseDTO product1 = buildProductDTO();
        ProductResponseDTO product2 = buildProductDTO();

        List<ProductResponseDTO> products = List.of(product1, product2);

        when(inventoryServiceClient.getProductsByInventoryName(inventoryName))
                .thenReturn(Flux.fromIterable(products));

        client.get()
                .uri("/api/gateway/inventories/{inventoryName}/products/by-name", inventoryName)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].productId").isEqualTo(product1.getProductId())
                .jsonPath("$[1].productId").isEqualTo(product2.getProductId());

        verify(inventoryServiceClient, times(1)).getProductsByInventoryName(inventoryName);
    }

    @Test
    void getProductsByInventoryName_withoutProducts_shouldReturnNotFound(){
        String inventoryName = "nonExistentInventory";

        when(inventoryServiceClient.getProductsByInventoryName(inventoryName))
                .thenReturn(Flux.empty());

        client.get()
                .uri("/api/gateway/inventories/{inventoryName}/products/by-name", inventoryName)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(inventoryServiceClient, times(1)).getProductsByInventoryName(inventoryName);
    }

    @Test
    void addInventoryType_withValidRequest_shouldReturnCreated() {

        InventoryTypeRequestDTO requestDTO = new InventoryTypeRequestDTO("External");

        requestDTO.setType("Internal");

        InventoryTypeResponseDTO responseDTO = InventoryTypeResponseDTO.builder()
                .typeId("type123")
                .type("Internal")
                .build();

        when(inventoryServiceClient.addInventoryType(any(InventoryTypeRequestDTO.class)))
                .thenReturn(Mono.just(responseDTO));

        client.post()
                .uri("/api/gateway/inventories/type")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.typeId").isEqualTo(responseDTO.getTypeId())
                .jsonPath("$.type").isEqualTo(responseDTO.getType());

        verify(inventoryServiceClient, times(1)).addInventoryType(any(InventoryTypeRequestDTO.class));



    }

    @Test
    void addInventoryType_withInvalidRequest_shouldReturnBadRequest() {
        InventoryTypeRequestDTO requestDTO = new InventoryTypeRequestDTO("");
        requestDTO.setType("");

        when(inventoryServiceClient.addInventoryType(any(InventoryTypeRequestDTO.class)))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/api/gateway/inventories/type")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isBadRequest();
        verify(inventoryServiceClient, times(1)).addInventoryType(any(InventoryTypeRequestDTO.class));
    }

    @Test
    void updateImportantStatus_withValidData_shouldSucceed() {
        String inventoryId = "dfa0a7e3-5a40-4b86-881e-9549ecda5e4b";
        Map<String, Boolean> request = Map.of("important", true);

        when(inventoryServiceClient.updateImportantStatus(eq(inventoryId), eq(true)))
                .thenReturn(Mono.empty());

        client.patch()
                .uri(baseInventoryURL + "/" + inventoryId + "/important")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        verify(inventoryServiceClient, times(1))
                .updateImportantStatus(eq(inventoryId), eq(true));
    }

    @Test
    void updateImportantStatus_withInvalidInventoryId_shouldReturnNotFound() {
        String invalidId = "invalid-id";
        Map<String, Boolean> request = Map.of("important", false);

        when(inventoryServiceClient.updateImportantStatus(eq(invalidId), eq(false)))
                .thenReturn(Mono.error(new RuntimeException("Inventory not found")));

        client.patch()
                .uri(baseInventoryURL + "/" + invalidId + "/important")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void addInventory_shouldReturnInventoryWithCode() {
        InventoryRequestDTO requestDTO = InventoryRequestDTO.builder()
                .inventoryName("Gateway Test Inventory")
                .inventoryType("Internal")
                .inventoryDescription("Testing via gateway")
                .build();

        InventoryResponseDTO responseDTO = InventoryResponseDTO.builder()
                .inventoryId("generated_id")
                .inventoryCode("INV-0001")
                .inventoryName("Gateway Test Inventory")
                .inventoryType("Internal")
                .inventoryDescription("Testing via gateway")
                .build();

        when(inventoryServiceClient.addInventory(any(InventoryRequestDTO.class)))
                .thenReturn(Mono.just(responseDTO));

        client.post()
                .uri(baseInventoryURL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(InventoryResponseDTO.class)
                .value(response -> {
                    assertNotNull(response.getInventoryCode());
                    assertEquals("INV-0001", response.getInventoryCode());
                });

        verify(inventoryServiceClient, times(1)).addInventory(any(InventoryRequestDTO.class));
    }

    @Test
    void getInventoryById_shouldReturnInventoryWithCode() {
        String inventoryId = "test_id";

        InventoryResponseDTO responseDTO = InventoryResponseDTO.builder()
                .inventoryId(inventoryId)
                .inventoryCode("INV-0042")
                .inventoryName("Test Inventory")
                .inventoryType("Internal")
                .build();

        when(inventoryServiceClient.getInventoryById(inventoryId))
                .thenReturn(Mono.just(responseDTO));

        client.get()
                .uri(baseInventoryURL + "/{inventoryId}", inventoryId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryResponseDTO.class)
                .value(response -> {
                    assertNotNull(response.getInventoryCode());
                    assertEquals("INV-0042", response.getInventoryCode());
                });

        verify(inventoryServiceClient, times(1)).getInventoryById(inventoryId);
    }

    @Test
    void getAllInventories_withValidInventoryCode_shouldReturnInventory() {
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(2);
        String inventoryCode = "INV-0001";

        when(inventoryServiceClient.searchInventory(page, size, inventoryCode, null, null, null, null))
                .thenReturn(Flux.just(buildInventoryDTO()));

        client.get()
                .uri(baseInventoryURL + "?page=0&size=2&inventoryCode=" + inventoryCode)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(1);

        verify(inventoryServiceClient, times(1))
                .searchInventory(eq(page), eq(size), eq(inventoryCode), eq(null), eq(null), eq(null), eq(null));
    }

    @Test
    void getAllInventories_withInvalidInventoryCode_shouldReturnEmpty() {
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(2);
        String invalidCode = "INV-9999";

        when(inventoryServiceClient.searchInventory(page, size, invalidCode, null, null, null, null))
                .thenReturn(Flux.empty());

        client.get()
                .uri(baseInventoryURL + "?page=0&size=2&inventoryCode=" + invalidCode)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .hasSize(0);

        verify(inventoryServiceClient, times(1))
                .searchInventory(eq(page), eq(size), eq(invalidCode), eq(null), eq(null), eq(null), eq(null));
    }

    @Test
    void getInventoryById_shouldReturnInventoryWithRecentUpdateMessage() {
        String inventoryId = "test_id";

        InventoryResponseDTO responseDTO = InventoryResponseDTO.builder()
                .inventoryId(inventoryId)
                .inventoryCode("INV-0042")
                .inventoryName("Test Inventory")
                .inventoryType("Internal")
                .recentUpdateMessage("3 supplies updated in the last 15 min.")
                .build();

        when(inventoryServiceClient.getInventoryById(inventoryId))
                .thenReturn(Mono.just(responseDTO));

        client.get()
                .uri(baseInventoryURL + "/{inventoryId}", inventoryId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InventoryResponseDTO.class)
                .value(response -> {
                    assertNotNull(response.getRecentUpdateMessage());
                    assertEquals("3 supplies updated in the last 15 min.", response.getRecentUpdateMessage());
                });

        verify(inventoryServiceClient, times(1)).getInventoryById(inventoryId);
    }

    @Test
    void searchInventories_shouldReturnInventoriesWithRecentUpdateMessages() {
        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(2);

        InventoryResponseDTO inventory1 = buildInventoryDTO();
        inventory1.setRecentUpdateMessage("2 supplies updated in the last 15 min.");

        InventoryResponseDTO inventory2 = buildInventoryDTO();
        inventory2.setInventoryId("2");
        inventory2.setRecentUpdateMessage("No recent updates.");

        when(inventoryServiceClient.searchInventory(page, size, null, null, null, null, null))
                .thenReturn(Flux.just(inventory1, inventory2));

        client.get()
                .uri(baseInventoryURL + "?page=0&size=2")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InventoryResponseDTO.class)
                .value(list -> {
                    assertEquals(2, list.size());
                    assertNotNull(list.get(0).getRecentUpdateMessage());
                    assertNotNull(list.get(1).getRecentUpdateMessage());
                });

        verify(inventoryServiceClient, times(1))
                .searchInventory(eq(page), eq(size), eq(null), eq(null), eq(null), eq(null), eq(null));
    }
}