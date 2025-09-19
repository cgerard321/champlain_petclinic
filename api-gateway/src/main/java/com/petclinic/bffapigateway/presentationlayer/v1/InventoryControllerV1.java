package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.InventoryServiceClient;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/inventory")
public class InventoryControllerV1 {

    private final InventoryServiceClient inventoryServiceClient;


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(@RequestBody InventoryRequestDTO model){
        return inventoryServiceClient.addInventory(model)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PostMapping(value = "/type")
    public Mono<ResponseEntity<InventoryTypeResponseDTO>> addInventoryType(@RequestBody InventoryTypeRequestDTO inventoryTypeRequestDTO){
        return inventoryServiceClient.addInventoryType(inventoryTypeRequestDTO)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
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
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/types")
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes(){
        return inventoryServiceClient.getAllInventoryTypes();
    }

    //Start of Inventory Methods
    @GetMapping("/inventory/{inventoryId}/products-pagination")
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER, Roles.VET})
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductFieldPagination(@PathVariable String inventoryId,
                                                                                                 @RequestParam(required = false) String productName,
                                                                                                 @RequestParam(required = false) Double productPrice,
                                                                                                 @RequestParam(required = false) Integer productQuantity,
                                                                                                 @RequestParam Optional<Integer> page,
                                                                                                 @RequestParam Optional<Integer> size){
        return inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductFieldPagination(inventoryId, productName, productPrice, productQuantity, page, size);
    }

    @GetMapping("/inventory/{inventoryId}/products-count")
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    public Mono<ResponseEntity<Long>> getTotalNumberOfProductsWithRequestParams(@PathVariable String inventoryId,
                                                                                @RequestParam(required = false) String productName,
                                                                                @RequestParam(required = false) Double productPrice,
                                                                                @RequestParam(required = false) Integer productQuantity){
        return inventoryServiceClient.getTotalNumberOfProductsWithRequestParams(inventoryId, productName, productPrice, productQuantity)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
    @PostMapping(value = "inventory/{inventoryId}/products")
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    public Mono<ResponseEntity<ProductResponseDTO>> addProductToInventory(@RequestBody ProductRequestDTO model, @PathVariable String inventoryId){
        return inventoryServiceClient.addProductToInventory(model, inventoryId)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }




    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})

    @GetMapping(value ="inventory/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(@PathVariable String inventoryId){
        return inventoryServiceClient.getInventoryById(inventoryId)
                .map(inventory -> ResponseEntity.status(HttpStatus.OK).body(inventory))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping(value ="inventory/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByProductIdInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.getProductByProductIdInInventory(inventoryId, productId)
                .map(product ->ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PutMapping(value = "inventory/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory( @RequestBody InventoryRequestDTO model, @PathVariable String inventoryId) {
        return inventoryServiceClient.updateInventory(model, inventoryId)
                .map(updatedStudent -> ResponseEntity.status(HttpStatus.OK).body(updatedStudent))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }



    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PutMapping(value = "inventory/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(@RequestBody ProductRequestDTO model, @PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.updateProductInInventory(model, inventoryId, productId)
                .map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "inventory/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProductInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.deleteProductInInventory(inventoryId, productId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping(value = "inventory/{inventoryId}/products")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndFields(@PathVariable String inventoryId,
                                                                                 @RequestParam(required = false) String productName,
                                                                                 @RequestParam(required = false) Double productPrice,
                                                                                 @RequestParam(required = false) Integer productQuantity,
                                                                                 @RequestParam(required = false) Double productSalePrice){
        return inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductsField(inventoryId, productName, productPrice, productQuantity, productSalePrice);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET})
    @PatchMapping("inventory/{inventoryId}/products/{productId}/consume")
    public Mono<ResponseEntity<ProductResponseDTO>> consumeProduct(
            @PathVariable String inventoryId,
            @PathVariable String productId) {

        return inventoryServiceClient.consumeProduct(inventoryId, productId)
                .map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }




    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping(value = "inventory")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
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
    @DeleteMapping(value = "inventory")
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return inventoryServiceClient.deleteAllInventories().then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "inventory/{inventoryId}")
    public Mono<Void> deleteInventoryByInventoryId(@PathVariable String inventoryId) {
        return inventoryServiceClient.deleteInventoryByInventoryId(inventoryId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @GetMapping(value="inventory/{inventoryId}/products/lowstock")
    public Flux<ProductResponseDTO>getLowStockProducts(@PathVariable String inventoryId, @RequestParam Optional<Integer> threshold){
        int stockThreshold = threshold.orElse(20);
        return inventoryServiceClient.getLowStockProducts(inventoryId, stockThreshold);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping(value = "inventory/{inventoryId}/products/search")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> searchProducts(@PathVariable String inventoryId,
                                                   @RequestParam(required = false) String productName,
                                                   @RequestParam(required = false) String productDescription,
                                                   @RequestParam(required = false) Status status) {
        return inventoryServiceClient.searchProducts(inventoryId, productName, productDescription, status);
    }




}
