package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.presentationlayer.ProductRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductResponseDTO;
import reactor.core.publisher.Mono;

public interface ProductInventoryService {
    Mono<ProductResponseDTO> addProductToInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId);
    Mono<ProductResponseDTO> updateProductInInventory(Mono<ProductRequestDTO> productRequestDTOMono, String inventoryId, String productId);

}
