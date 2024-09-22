package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.presentationlayer.*;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SupplyInventoryService {

    Mono<SupplyResponseDTO> addSupplyToInventory(Mono<SupplyRequestDTO> supplyRequestDTOMono, String inventoryId);
    Mono<InventoryResponseDTO> addInventory(Mono<InventoryRequestDTO> inventoryRequestDTO);
    Mono<InventoryResponseDTO> updateInventory(Mono<InventoryRequestDTO> inventoryRequestDTO, String inventoryId);

    Mono<InventoryResponseDTO> getInventoryById(String inventoryId);
    Mono<SupplyResponseDTO> updateSupplyInInventory(Mono<SupplyRequestDTO> supplyRequestDTOMono, String inventoryId, String supplyId);
    Mono<Void> deleteSupplyInInventory(String inventoryId, String supplyId);
    Flux<SupplyResponseDTO> getSuppliesInInventoryByInventoryIdAndSuppliesField(String inventoryId, String supplyName, Double supplyPrice, Integer supplyQuantity, Double supplySalePrice);
    Flux<SupplyResponseDTO> getSuppliesInInventoryByInventoryIdAndSuppliesFieldsPagination(String inventoryId, String supplyName, Double supplyPrice, Integer supplyQuantity, Pageable pageable);

    Mono<Void> deleteInventoryByInventoryId(String inventoryId);

    Mono<Void> deleteAllSupplyInventory(String inventoryId);
    Mono<Void> deleteAllInventory();
    Mono<InventoryTypeResponseDTO> addInventoryType(Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO);

    Flux<InventoryResponseDTO> searchInventories(Pageable page, String inventoryName, String inventoryType, String inventoryDescription);
    Flux<InventoryTypeResponseDTO> getAllInventoryTypes();
    Flux<InventoryNameResponseDTO> getAllInventoryNames();

    Mono<SupplyResponseDTO> getSupplyBySupplyIdInInventory(String inventoryId, String supplyId);

    Mono<InventoryResponseDTO> addSupplyToInventoryByInventoryName(String inventoryName, Mono<SupplyRequestDTO> supplyRequestDTOMono);



}
