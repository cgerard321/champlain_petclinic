package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.presentationlayer.*;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ProductInventoryService {
    Mono<ProductResponseDTO> addProductToInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId);
    Mono<InventoryResponseDTO> addInventory(Mono<InventoryRequestDTO> inventoryRequestDTO);
    Mono<InventoryResponseDTO> updateInventory(Mono<InventoryRequestDTO> inventoryRequestDTO, String inventoryId);

    Mono<InventoryResponseDTO> getInventoryById(String inventoryId);
    Mono<ProductResponseDTO> updateProductInInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId, String productId);
    Mono<Void> deleteProductInInventory(String inventoryId, String productId);
    Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductsField(String inventoryId, String productName, Double productPrice, Integer productQuantity, Double productSalePrice);
   // Flux<InventoryResponseDTO> getAllInventory();

    Mono<Void> deleteInventoryByInventoryId(String inventoryId);

    Mono<Void> deleteAllProductInventory(String inventoryId);
    Mono<Void> deleteAllInventory();
    Mono<InventoryTypeResponseDTO> addInventoryType(Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO);

    Flux<InventoryResponseDTO> searchInventories(Pageable page, String inventoryName, String inventoryType, String inventoryDescription);
    Flux<InventoryTypeResponseDTO> getAllInventoryTypes();

    Mono<ProductResponseDTO> getProductByProductIdInInventory(String inventoryId, String productId);


}
