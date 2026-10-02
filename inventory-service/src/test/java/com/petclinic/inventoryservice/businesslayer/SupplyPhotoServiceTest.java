package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.ProductRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import com.petclinic.inventoryservice.utils.InventoryValidator;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SupplyPhotoServiceTest {
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private InventoryRepository inventories;
    private ProductRepository products;
    private ProductInventoryService service;
    private Product existing;

    @BeforeEach
    void setUp() {
        inventories = mock(InventoryRepository.class);
        products = mock(ProductRepository.class);
        InventoryTypeRepository types = mock(InventoryTypeRepository.class);
        service = new ProductInventoryServiceImpl(inventories, products, types,
                new InventoryValidator(inventories, types, products));
        existing = Product.builder().productId("supply-1").inventoryId("inventory-1")
                .productName("Original supply").productQuantity(10).productPrice(5.0)
                .photoData(JPG.clone()).photoType("image/jpeg").build();
        when(inventories.findInventoryByInventoryId("inventory-1"))
                .thenReturn(Mono.just(new Inventory()));
        when(products.findProductByProductId("supply-1")).thenReturn(Mono.just(existing));
        when(products.existsByInventoryIdAndProductNameIgnoreCase("inventory-1", "Supply"))
                .thenReturn(Mono.just(false));
        when(products.existsByInventoryIdAndProductNameIgnoreCaseAndProductIdNot(
                "inventory-1", "Supply", "supply-1")).thenReturn(Mono.just(false));
        when(products.save(any(Product.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    private ProductRequestDTO request(byte[] data, String type) {
        return ProductRequestDTO.builder().productName("Supply").productDescription("Updated supply")
                .productPrice(10.0).productQuantity(25).productSalePrice(12.0)
                .photoData(data).photoType(type).build();
    }

    static Stream<Arguments> validPhotos() {
        return Stream.of(Arguments.of(JPG, "image/jpeg"), Arguments.of(PNG, "image/png"),
                Arguments.of(Arrays.copyOf(JPG, 2 * 1024 * 1024), "image/jpeg"));
    }

    @ParameterizedTest
    @MethodSource("validPhotos")
    void addSupplyPersistsValidPhotoAndMapsResponse(byte[] data, String type) {
        StepVerifier.create(service.addSupplyToInventory(Mono.just(request(data, type)), "inventory-1"))
                .assertNext(response -> {
                    assertPhoto(response, data, type);
                    assertEquals("inventory-1", response.getInventoryId());
                    assertNotNull(response.getProductId());
                }).verifyComplete();
        verify(products).save(argThat(saved -> Arrays.equals(data, saved.getPhotoData())
                && type.equals(saved.getPhotoType())));
    }

    @Test
    void updatePreservesExistingPhotoWhenDataIsOmitted() {
        assertUpdatePhoto(null, null, JPG, "image/jpeg");
        assertEquals("Supply", existing.getProductName());
        assertEquals(25, existing.getProductQuantity());
    }

    @ParameterizedTest
    @MethodSource("validPhotos")
    void updateReplacesExistingPhoto(byte[] data, String type) {
        assertUpdatePhoto(data, type, data, type);
    }

    @Test
    void updateRemovesPhotoWithEmptyData() {
        assertUpdatePhoto(new byte[0], null, null, null);
    }

    @Test
    void addSupplyNormalizesEmptyPhotoToNull() {
        StepVerifier.create(service.addSupplyToInventory(Mono.just(request(new byte[0], " ")), "inventory-1"))
                .assertNext(response -> assertPhoto(response, null, null)).verifyComplete();
        verify(products).save(argThat(saved -> saved.getPhotoData() == null && saved.getPhotoType() == null));
    }

    private void assertUpdatePhoto(byte[] data, String type, byte[] expectedData, String expectedType) {
        StepVerifier.create(service.updateProductInInventory(Mono.just(request(data, type)),
                        "inventory-1", "supply-1"))
                .assertNext(response -> assertPhoto(response, expectedData, expectedType)).verifyComplete();
        verify(products).save(same(existing));
        assertArrayEquals(expectedData, existing.getPhotoData());
        assertEquals(expectedType, existing.getPhotoType());
    }

    static Stream<Arguments> invalidPhotos() {
        return Stream.of(
                Arguments.of(new byte[]{'G', 'I', 'F', '8'}, "image/gif", "Only JPG and PNG photos are supported."),
                Arguments.of(JPG, "image/png", "Only JPG and PNG photos are supported."),
                Arguments.of(PNG, "image/jpeg", "Only JPG and PNG photos are supported."),
                Arguments.of(JPG, null, "Only JPG and PNG photos are supported."),
                Arguments.of(new byte[]{(byte) 0xFF, (byte) 0xD8}, "image/jpeg", "Only JPG and PNG photos are supported."),
                Arguments.of(new byte[8], "image/png", "Only JPG and PNG photos are supported."),
                Arguments.of(Arrays.copyOf(JPG, 2 * 1024 * 1024 + 1), "image/jpeg", "Photo must be 2 MB or smaller."),
                Arguments.of(null, "image/jpeg", "Photo data is missing."),
                Arguments.of(new byte[0], "image/png", "Photo data is missing."));
    }

    @ParameterizedTest
    @MethodSource("invalidPhotos")
    void addSupplyRejectsInvalidPhotoWithoutSaving(byte[] data, String type, String message) {
        StepVerifier.create(service.addSupplyToInventory(Mono.just(request(data, type)), "inventory-1"))
                .expectErrorMatches(error -> error instanceof InvalidInputException
                        && message.equals(error.getMessage())).verify();
        verify(products, never()).save(any(Product.class));
    }

    @ParameterizedTest
    @MethodSource("invalidPhotos")
    void updateRejectsInvalidPhotoWithoutChangingExistingPhoto(byte[] data, String type, String message) {
        StepVerifier.create(service.updateProductInInventory(Mono.just(request(data, type)),
                        "inventory-1", "supply-1"))
                .expectErrorMatches(error -> error instanceof InvalidInputException
                        && message.equals(error.getMessage())).verify();
        verify(products, never()).save(any(Product.class));
        assertArrayEquals(JPG, existing.getPhotoData());
        assertEquals("image/jpeg", existing.getPhotoType());
    }

    private static void assertPhoto(ProductResponseDTO response, byte[] data, String type) {
        assertArrayEquals(data, response.getPhotoData());
        assertEquals(type, response.getPhotoType());
    }
}
