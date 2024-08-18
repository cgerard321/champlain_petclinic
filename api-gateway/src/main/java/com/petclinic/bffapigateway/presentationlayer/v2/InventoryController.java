package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.InventoryServiceClient;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryTypeResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import io.swagger.annotations.ResponseHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/inventories")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class InventoryController {
    private final InventoryServiceClient inventoryServiceClient;
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping()//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseHeader(name = "Search Inventory & Get All Inventories pagination", description = "Total number of items available")
    public Flux<InventoryResponseDTO> searchInventory(@RequestParam Optional<Integer> page,
                                                      @RequestParam Optional<Integer> size,
                                                      @RequestParam(required = false) String inventoryName,
                                                      @RequestParam(required = false) String inventoryType,
                                                      @RequestParam(required = false) String inventoryDescription){
        if(page.isEmpty()){
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(10);
        }
        return inventoryServiceClient.searchInventory(page, size, inventoryName, inventoryType, inventoryDescription);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/types")
    @ResponseHeader(name = "Get All Inventory Types", description = "All available inventory types")
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes(){
        return inventoryServiceClient.getAllInventoryTypes();
    }

    @DeleteMapping(value = "/{inventoryId}")
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @ResponseHeader(name = "Delete Inventory by Inventory Id", description = "Deletes an inventory by inventory id")
    public Mono<Void> deleteInventoryByInventoryId(@PathVariable String inventoryId) {
        return inventoryServiceClient.deleteInventoryByInventoryId(inventoryId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "")
    @ResponseHeader(name = "Delete All Inventories", description = "Deletes all inventories")
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return inventoryServiceClient.deleteAllInventories().then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
