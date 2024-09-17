package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.InventoryServiceClient;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryTypeResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.SupplyRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.SupplyResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET})
    @GetMapping()//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiResponses(value = {@ApiResponse(useReturnTypeSchema = true, description = "Total number of items available", responseCode = "200")})
    public ResponseEntity<Flux<InventoryResponseDTO>> searchInventory(@RequestParam Optional<Integer> page,
                                                                      @RequestParam Optional<Integer> size,
                                                                      @RequestParam(required = false) String inventoryName,
                                                                      @RequestParam(required = false) String inventoryType,
                                                                      @RequestParam(required = false) String inventoryDescription) {
        if (page.isEmpty()) {
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(10);
        }
        return ResponseEntity.ok().body(inventoryServiceClient.searchInventory(page, size, inventoryName, inventoryType, inventoryDescription));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/types")
    @ApiResponses(value = {@ApiResponse(description = "All available inventory types", responseCode = "200")})
    public ResponseEntity<Flux<InventoryTypeResponseDTO>> getAllInventoryTypes() {
        return ResponseEntity.ok().body(inventoryServiceClient.getAllInventoryTypes());
    }

    @DeleteMapping(value = "/{inventoryId}")
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @ApiResponses(value = {@ApiResponse(description = "Deletes an inventory by inventory id", responseCode = "204"), @ApiResponse(description = "Deletes an inventory by invalid inventory id", responseCode = "404")})
    public Mono<ResponseEntity<Void>> deleteInventoryByInventoryId(@PathVariable String inventoryId) {
        try {
            return inventoryServiceClient.deleteInventoryByInventoryId(inventoryId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return Mono.just(ResponseEntity.notFound().build());
        }
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "")
    @ApiResponses(value = {@ApiResponse(useReturnTypeSchema = true, description = "Deletes all inventories", responseCode = "204")})
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return inventoryServiceClient.deleteAllInventories().then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /*
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping("/{inventoryType}/supplies")
    @ApiResponses(value = {
            @ApiResponse(description = "Add a supply to inventory by type", responseCode = "201"),
            @ApiResponse(description = "Inventory type not found", responseCode = "404")
    })
    public Mono<ResponseEntity<InventoryResponseDTO>> addSupplyToInventoryByType(
            @PathVariable String inventoryType,
            @RequestBody Mono<SupplyRequestDTO> supplyRequestDTOMono) {

        return supplyRequestDTOMono.flatMap(supplyRequestDTO ->
                inventoryServiceClient.addSupplyToInventoryByType(inventoryType, supplyRequestDTO)
                        .map(inventoryResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(inventoryResponseDTO))
                        .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()))
        );
    }
*/


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping("/{inventoryType}/supplies")
    @ApiResponses(value = {
            @ApiResponse(description = "Add a supply to inventory by type", responseCode = "201"),
            @ApiResponse(description = "Inventory type not found", responseCode = "404")
    })
    public Mono<ResponseEntity<InventoryResponseDTO>> addSupplyToInventoryByType(
            @PathVariable String inventoryType,
            @RequestBody Mono<SupplyRequestDTO> supplyRequestDTOMono) {

        return supplyRequestDTOMono.flatMap(supplyRequestDTO ->
                inventoryServiceClient.addSupplyToInventoryByType(inventoryType, supplyRequestDTO)
                        .map(inventoryResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(inventoryResponseDTO))
                        .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()))
        );
    }



}
