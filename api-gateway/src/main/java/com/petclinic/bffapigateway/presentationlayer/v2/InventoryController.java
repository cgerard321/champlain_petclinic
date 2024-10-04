package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.InventoryServiceClient;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/names")
    @ApiResponses(value = {@ApiResponse(description = "All available inventory names", responseCode = "200")})
    public ResponseEntity<Flux<InventoryNameResponseDTO>> getAllInventoryNames() {
        return ResponseEntity.ok().body(inventoryServiceClient.getAllInventoryNames());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping(value = "/types")
    @ApiResponses(value = {@ApiResponse(description = "Creates a new inventory type", responseCode = "201"), @ApiResponse(description = "Creates a new inventory type with invalid data", responseCode = "400")})
    public Mono<ResponseEntity<InventoryTypeResponseDTO>> createInventoryType(@RequestBody InventoryTypeRequestDTO inventoryTypeRequestDTO) {
        return inventoryServiceClient.addInventoryType(inventoryTypeRequestDTO)
                .map(inventoryTypeResponseDTO -> ResponseEntity.status(201).body(inventoryTypeResponseDTO))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
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

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProductInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.deleteProductInInventory(inventoryId, productId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "")
    @ApiResponses(value = {@ApiResponse(useReturnTypeSchema = true, description = "Deletes all inventories", responseCode = "204")})
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return inventoryServiceClient.deleteAllInventories().then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping(value = "/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory(
            @PathVariable String inventoryId,
            @RequestBody InventoryRequestDTO inventoryRequestDTO) {

        return Mono.just(inventoryId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided inventory ID is invalid: " + inventoryId)))
                .flatMap(id -> inventoryServiceClient.updateInventory( inventoryRequestDTO,id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "{inventoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(@PathVariable String inventoryId) {
        return inventoryServiceClient.getInventoryById(inventoryId)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping("/{inventoryName}/products/by-name")
    @ApiResponses(value = {
            @ApiResponse(description = "Add a product to inventory by Name", responseCode = "201"),
            @ApiResponse(description = "Inventory name not found", responseCode = "404")
    })
    public Mono<ResponseEntity<InventoryResponseDTO>> addProductToInventoryByName(
            @PathVariable String inventoryName,
            @RequestBody Mono<ProductRequestDTO> productRequestDTOMono) {

        return productRequestDTOMono.flatMap(productRequestDTO ->
                inventoryServiceClient.addProductToInventoryByName(inventoryName, productRequestDTO)
                        .map(inventoryResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(inventoryResponseDTO))
                        .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()))
        );
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping("/{inventoryName}/products/by-name")
    @ApiResponses(value = {
            @ApiResponse(description = "Get products by inventory name", responseCode = "200"),
            @ApiResponse(description = "Inventory name not found", responseCode = "404")
    })
    public Mono<ResponseEntity<Flux<ProductResponseDTO>>> getProductsByInventoryName(@PathVariable String inventoryName) {
        return inventoryServiceClient.getProductsByInventoryName(inventoryName)
                .collectList()
                .map(productResponseDTOS -> {
                    if (productResponseDTOS.isEmpty()) {
                        return ResponseEntity.notFound().build();
                    } else {
                        return ResponseEntity.ok(Flux.fromIterable(productResponseDTOS));
                    }
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(@RequestBody InventoryRequestDTO inventoryRequestDTO) {
        return inventoryServiceClient.addInventory(inventoryRequestDTO)
                .map(inventory -> ResponseEntity.status(HttpStatus.CREATED).body(inventory))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}/products/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Flux<ProductResponseDTO>>> searchProducts(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productDescription) {

        Flux<ProductResponseDTO> products = inventoryServiceClient
                .searchProducts(inventoryId, productName, productDescription);

        return products
                .hasElements() // Check if any elements are present in the flux
                .flatMap(hasElements -> {
                    if (hasElements) {
                        return Mono.just(ResponseEntity.ok(products));
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a product by its ID in an inventory")
    @ApiResponses(value = {
            @ApiResponse(description = "Product found", responseCode = "200"),
            @ApiResponse(description = "Product not found", responseCode = "404")
    })
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByProductIdInInventory(
            @PathVariable String inventoryId,
            @PathVariable String productId) {

        return inventoryServiceClient.getProductByProductIdInInventory(inventoryId, productId)
                .map(productResponseDTO -> ResponseEntity.ok(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping(value = "/{inventoryId}/products/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a product in an inventory")
    @ApiResponses(value = {
            @ApiResponse(description = "Product updated successfully", responseCode = "200"),
            @ApiResponse(description = "Invalid inputs or product not found", responseCode = "400")
    })
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(
            @RequestBody ProductRequestDTO productRequestDTO,
            @PathVariable String inventoryId,
            @PathVariable String productId) {

        return inventoryServiceClient.updateProductInInventory(productRequestDTO, inventoryId, productId)
                .map(productResponseDTO -> ResponseEntity.ok(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping("/{inventoryId}/products")
    public Mono<ResponseEntity<ProductResponseDTO>> addSupplyToInventory(
            @RequestBody ProductRequestDTO productRequestDTO,
            @PathVariable String inventoryId) {
        return inventoryServiceClient.addSupplyToInventory(productRequestDTO, inventoryId)
                .map(productResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping("/{inventoryId}/productquantity")
    public Mono<ResponseEntity<Integer>> getQuantityOfProductsInInventory(
            @PathVariable String inventoryId){
        return inventoryServiceClient.getQuantityOfProductsInInventory(inventoryId)
                .map(quantity -> ResponseEntity.ok(quantity))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }   
    

    }
}
