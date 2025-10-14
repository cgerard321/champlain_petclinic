package com.petclinic.bffapigateway.presentationlayer.v1;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/inventories")
public class InventoryControllerV1 {

    private final InventoryServiceClient inventoryServiceClient;


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(@RequestBody InventoryRequestDTO model){
        return inventoryServiceClient.addInventory(model)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

//     return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build());

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
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/types")
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes(){
        return inventoryServiceClient.getAllInventoryTypes();
    }

    //Start of Inventory Methods
    @GetMapping("/{inventoryId}/products-pagination")
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER, Roles.VET})
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductFieldPagination(@PathVariable String inventoryId,
                                                                                                 @RequestParam(required = false) String productName,
                                                                                                 @RequestParam(required = false) Double productPrice,
                                                                                                 @RequestParam(required = false) Integer productQuantity,
                                                                                                 @RequestParam Optional<Integer> page,
                                                                                                 @RequestParam Optional<Integer> size){
        return inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductFieldPagination(inventoryId, productName, productPrice, productQuantity, page, size);
    }

    @GetMapping("/{inventoryId}/products-count")
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    public Mono<ResponseEntity<Long>> getTotalNumberOfProductsWithRequestParams(@PathVariable String inventoryId,
                                                                                @RequestParam(required = false) String productName,
                                                                                @RequestParam(required = false) Double productPrice,
                                                                                @RequestParam(required = false) Integer productQuantity){
        return inventoryServiceClient.getTotalNumberOfProductsWithRequestParams(inventoryId, productName, productPrice, productQuantity)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

//    @PostMapping(value = "/{inventoryId}/products")
//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
//    public Mono<ResponseEntity<ProductResponseDTO>> addProductToInventory(@RequestBody ProductRequestDTO model, @PathVariable String inventoryId){
//        return inventoryServiceClient.addProductToInventory(model, inventoryId)
//                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    } AddSupplyToInventory already does this




    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(@PathVariable String inventoryId) {
        return inventoryServiceClient.getInventoryById(inventoryId)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "Get a product by its ID in an inventory")
//    @ApiResponses(value = {
//            @ApiResponse(description = "Product found", responseCode = "200"),
//            @ApiResponse(description = "Product not found", responseCode = "404")
//    })
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByProductIdInInventory(
            @PathVariable String inventoryId,
            @PathVariable String productId) {

        return inventoryServiceClient.getProductByProductIdInInventory(inventoryId, productId)
                .map(productResponseDTO -> ResponseEntity.ok(productResponseDTO))
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
                .flatMap(id -> inventoryServiceClient.updateInventory(inventoryRequestDTO, id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }



    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping(value = "/{inventoryId}/products/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a product in an inventory")
//    @ApiResponses(value = {
//            @ApiResponse(description = "Product updated successfully", responseCode = "200"),
//            @ApiResponse(description = "Invalid inputs or product not found", responseCode = "400")
//    })
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(
            @RequestBody ProductRequestDTO productRequestDTO,
            @PathVariable String inventoryId,
            @PathVariable String productId) {

        return inventoryServiceClient.updateProductInInventory(productRequestDTO, inventoryId, productId)
                .map(productResponseDTO -> ResponseEntity.ok(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProductInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return inventoryServiceClient.deleteProductInInventory(inventoryId, productId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping(value = "/{inventoryId}/products")//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndFields(@PathVariable String inventoryId,
                                                                                 @RequestParam(required = false) String productName,
                                                                                 @RequestParam(required = false) Double productPrice,
                                                                                 @RequestParam(required = false) Integer productQuantity,
                                                                                 @RequestParam(required = false) Double productSalePrice){
        return inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductsField(inventoryId, productName, productPrice, productQuantity, productSalePrice);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET})
    @PatchMapping("/{inventoryId}/products/{productId}/consume")
    public Mono<ResponseEntity<ProductResponseDTO>> consumeProduct(
            @PathVariable String inventoryId,
            @PathVariable String productId) {

        return inventoryServiceClient.consumeProduct(inventoryId, productId)
                .map(s -> ResponseEntity.status(HttpStatus.OK).body(s))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }






    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @DeleteMapping()
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return inventoryServiceClient.deleteAllInventories().then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "/{inventoryId}/products")
    public Mono<ResponseEntity<Void>> deleteAllProductsFromInventory(@PathVariable String inventoryId) {
        return inventoryServiceClient.deleteAllProductsInInventory(inventoryId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/{inventoryId}")
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
//    @ApiResponses(value = {@ApiResponse(description = "Deletes an inventory by inventory id", responseCode = "204"), @ApiResponse(description = "Deletes an inventory by invalid inventory id", responseCode = "404")})
    public Mono<ResponseEntity<Void>> deleteInventoryByInventoryId(@PathVariable String inventoryId) {
        try {
            return inventoryServiceClient.deleteInventoryByInventoryId(inventoryId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return Mono.just(ResponseEntity.notFound().build());
        }
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @GetMapping(value="/{inventoryId}/products/lowstock")
    public Flux<ProductResponseDTO>getLowStockProducts(@PathVariable String inventoryId, @RequestParam Optional<Integer> threshold){
        int stockThreshold = threshold.orElse(20);
        return inventoryServiceClient.getLowStockProducts(inventoryId, stockThreshold);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}/products/search")
    public Mono<ResponseEntity<Flux<ProductResponseDTO>>> searchProducts(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productDescription,
            @RequestParam(required = false) Status status) {

        Flux<ProductResponseDTO> products = inventoryServiceClient
                .searchProducts(inventoryId, productName, productDescription, status);

        return products
                .hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) {
                        return Mono.just(ResponseEntity.ok(products));
                    } else {
                        return Mono.just(ResponseEntity.noContent().build());
                    }
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping("/{inventoryId}/products/download")
    public Mono<ResponseEntity<byte[]>> createSupplyPdf(@PathVariable String inventoryId) {
        return inventoryServiceClient.createSupplyPdf(inventoryId)
                .map(pdfContent -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory_report.pdf\"");
                    return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping("/{inventoryName}/products/by-name")
//    @ApiResponses(value = {
//            @ApiResponse(description = "Get products by inventory name", responseCode = "200"),
//            @ApiResponse(description = "Inventory name not found", responseCode = "404")
//    })
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

    @GetMapping("/all")
    public Flux<InventoryResponseDTO> getAllInventories() {
        return inventoryServiceClient.getAllInventories();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping("/{inventoryId}/products/{productId}/restockProduct")
    public Mono<ResponseEntity<ProductResponseDTO>> restockLowStockProduct (@PathVariable String inventoryId, @PathVariable String productId, @RequestParam Integer productQuantity){
        if (productQuantity == null || productQuantity <= 0) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        return inventoryServiceClient.restockLowStockProduct(inventoryId, productId, productQuantity)
                .map(updatedProduct -> ResponseEntity.status(HttpStatus.OK).body(updatedProduct))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping("/{inventoryId}/productquantity")
    public Mono<ResponseEntity<Integer>> getQuantityOfProductsInInventory(
            @PathVariable String inventoryId) {
        return inventoryServiceClient.getQuantityOfProductsInInventory(inventoryId)
                .map(quantity -> ResponseEntity.ok(quantity))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER})
    @PatchMapping("/{inventoryId}/important")
    public Mono<ResponseEntity<Object>> updateImportantStatus(@PathVariable String inventoryId, @RequestBody Map<String, Boolean> request) {
        Boolean important = request.get("important");
        return inventoryServiceClient.updateImportantStatus(inventoryId, important)
                .then(Mono.just(ResponseEntity.ok().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.INVENTORY_MANAGER,Roles.VET})
    @GetMapping()//, produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryResponseDTO> searchInventory(@RequestParam Optional<Integer> page,
                                                      @RequestParam Optional<Integer> size,
                                                      @RequestParam(required = false) String inventoryCode,
                                                      @RequestParam(required = false) String inventoryName,
                                                      @RequestParam(required = false) String inventoryType,
                                                      @RequestParam(required = false) String inventoryDescription,
                                                      @RequestParam(required = false) Boolean importantOnly){
        if(page.isEmpty()){
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(10);
        }
        return inventoryServiceClient.searchInventory(page, size, inventoryCode, inventoryName, inventoryType, inventoryDescription, importantOnly);
    }

        @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping("/{currentInventoryId}/products/{productId}/updateInventoryId/{newInventoryId}")
    @Operation(summary = "Update product's inventory ID in the inventory system")
    @ApiResponses(value = {
            @ApiResponse(description = "Inventory ID updated successfully", responseCode = "200"),
            @ApiResponse(description = "Product not found", responseCode = "404"),
            @ApiResponse(description = "Invalid inputs", responseCode = "400")
    })
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInventoryId(
            @PathVariable String currentInventoryId,
            @PathVariable String productId,
            @PathVariable String newInventoryId) {

        return inventoryServiceClient.updateProductInventoryId(currentInventoryId, productId, newInventoryId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }




}
