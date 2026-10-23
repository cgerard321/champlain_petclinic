package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.InventoryServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
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
import org.springframework.web.server.ServerWebExchange;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/inventories")
public class InventoryControllerV1 {

    private final InventoryServiceClient inventoryServiceClient;

    private String currentUsername(ServerWebExchange exchange) {
        TokenResponseDTO token = exchange.getAttribute("tokenValues");

        if (token == null || token.getUsername() == null || token.getUsername().isBlank()) {
            throw new IllegalStateException(
                    "Authenticated username is missing from the validated token.");
        }

        return token.getUsername();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(
            @RequestBody InventoryRequestDTO model
    ) {
        return inventoryServiceClient.addInventory(model)
                .map(inventory -> ResponseEntity.status(HttpStatus.CREATED).body(inventory))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping(value = "/type")
    public Mono<ResponseEntity<InventoryTypeResponseDTO>> addInventoryType(
            @RequestBody InventoryTypeRequestDTO inventoryTypeRequestDTO
    ) {
        return inventoryServiceClient.addInventoryType(inventoryTypeRequestDTO)
                .map(type -> ResponseEntity.status(HttpStatus.CREATED).body(type))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping("/{inventoryId}/products")
    public Mono<ResponseEntity<ProductResponseDTO>> addSupplyToInventory(
            @RequestBody ProductRequestDTO productRequestDTO,
            @PathVariable String inventoryId,
            ServerWebExchange exchange
    ) {
        return inventoryServiceClient.addSupplyToInventory(
                        productRequestDTO,
                        inventoryId,
                        currentUsername(exchange))
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product))
                .onErrorResume(NotFoundException.class,
                        error -> Mono.just(ResponseEntity.notFound().build()))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {
            Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET
    })
    @GetMapping(value = "/types", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes() {
        return inventoryServiceClient.getAllInventoryTypes();
    }

    @GetMapping(value = "/{inventoryId}/products-pagination",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SecuredEndpoint(allowedRoles = {
            Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET
    })
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductFieldPagination(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer productQuantity,
            @RequestParam(required = false) Double minSalePrice,
            @RequestParam(required = false) Double maxSalePrice,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size
    ) {
        return inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductFieldPagination(
                inventoryId,
                productName,
                minPrice,
                maxPrice,
                productQuantity,
                minSalePrice,
                maxSalePrice,
                page,
                size
        );
    }

    @GetMapping(value = "/{inventoryId}/products-count")
    @SecuredEndpoint(allowedRoles = {
            Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET
    })
    public Mono<ResponseEntity<Long>> getTotalNumberOfProductsWithRequestParams(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer productQuantity,
            @RequestParam(required = false) Double minSalePrice,
            @RequestParam(required = false) Double maxSalePrice
    ) {
        return inventoryServiceClient.getTotalNumberOfProductsWithRequestParams(
                        inventoryId,
                        productName,
                        minPrice,
                        maxPrice,
                        productQuantity,
                        minSalePrice,
                        maxSalePrice)
                .map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(
            @PathVariable String inventoryId
    ) {
        return inventoryServiceClient.getInventoryById(inventoryId)
                .map(inventory -> ResponseEntity.status(HttpStatus.OK).body(inventory))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}/products/{productId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByProductIdInInventory(
            @PathVariable String inventoryId,
            @PathVariable String productId
    ) {
        return inventoryServiceClient.getProductByProductIdInInventory(inventoryId, productId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping("/{inventoryId}/products/{productId}/history")
    public Flux<Map<String, Object>> getSupplyHistory(
            @PathVariable String inventoryId,
            @PathVariable String productId
    ) {
        return inventoryServiceClient.getSupplyHistory(inventoryId, productId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping(value = "/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory(
            @PathVariable String inventoryId,
            @RequestBody InventoryRequestDTO inventoryRequestDTO
    ) {
        return Mono.just(inventoryId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(
                        new InvalidInputException("Provided inventory ID is invalid: " + inventoryId)))
                .flatMap(id -> inventoryServiceClient.updateInventory(inventoryRequestDTO, id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping(value = "/{inventoryId}/products/{productId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a product in an inventory")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(
            @RequestBody ProductRequestDTO productRequestDTO,
            @PathVariable String inventoryId,
            @PathVariable String productId,
            ServerWebExchange exchange
    ) {
        return inventoryServiceClient.updateProductInInventory(
                        productRequestDTO,
                        inventoryId,
                        productId,
                        currentUsername(exchange))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProductInInventory(
            @PathVariable String inventoryId,
            @PathVariable String productId,
            ServerWebExchange exchange
    ) {
        return inventoryServiceClient.deleteProductInInventory(
                        inventoryId,
                        productId,
                        currentUsername(exchange))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {
            Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET
    })
    @GetMapping(value = "/{inventoryId}/products",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndFields(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer productQuantity,
            @RequestParam(required = false) Double minSalePrice,
            @RequestParam(required = false) Double maxSalePrice
    ) {
        return inventoryServiceClient.getProductsInInventoryByInventoryIdAndProductsField(
                inventoryId,
                productName,
                minPrice,
                maxPrice,
                productQuantity,
                minSalePrice,
                maxSalePrice
        );
    }

    @SecuredEndpoint(allowedRoles = {
            Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET
    })
    @PatchMapping("/{inventoryId}/products/{productId}/consume")
    public Mono<ResponseEntity<ProductResponseDTO>> consumeProduct(
            @PathVariable String inventoryId,
            @PathVariable String productId,
            ServerWebExchange exchange
    ) {
        return inventoryServiceClient.consumeProduct(
                        inventoryId,
                        productId,
                        currentUsername(exchange))
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @DeleteMapping
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return inventoryServiceClient.deleteAllInventories()
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "/{inventoryId}/products")
    public Mono<ResponseEntity<Void>> deleteAllProductsFromInventory(
            @PathVariable String inventoryId
    ) {
        return inventoryServiceClient.deleteAllProductsInInventory(inventoryId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "/{inventoryId}")
    public Mono<ResponseEntity<Void>> deleteInventoryByInventoryId(
            @PathVariable String inventoryId
    ) {
        try {
            return inventoryServiceClient.deleteInventoryByInventoryId(inventoryId)
                    .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } catch (Exception exception) {
            return Mono.just(ResponseEntity.notFound().build());
        }
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}/products/lowstock",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getLowStockProducts(
            @PathVariable String inventoryId,
            @RequestParam Optional<Integer> threshold
    ) {
        return inventoryServiceClient.getLowStockProducts(inventoryId, threshold.orElse(20));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryId}/products/search",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<ProductResponseDTO>>> searchProducts(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productDescription,
            @RequestParam(required = false) Status status
    ) {
        Flux<ProductResponseDTO> products =
                inventoryServiceClient.searchProducts(
                        inventoryId, productName, productDescription, status);

        return products.hasElements()
                .flatMap(hasProducts -> hasProducts
                        ? Mono.just(ResponseEntity.ok(products))
                        : Mono.just(ResponseEntity.noContent().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping("/{inventoryId}/products/download")
    public Mono<ResponseEntity<byte[]>> createSupplyPdf(
            @PathVariable String inventoryId
    ) {
        return inventoryServiceClient.createSupplyPdf(inventoryId)
                .map(pdfContent -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"inventory_report.pdf\""
                    );
                    return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping(value = "/{inventoryName}/products/by-name",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<ProductResponseDTO>>> getProductsByInventoryName(
            @PathVariable String inventoryName
    ) {
        return inventoryServiceClient.getProductsByInventoryName(inventoryName)
                .collectList()
                .map(products -> products.isEmpty()
                        ? ResponseEntity.notFound().build()
                        : ResponseEntity.ok(Flux.fromIterable(products)));
    }

    @GetMapping(value = "/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryResponseDTO> getAllInventories() {
        return inventoryServiceClient.getAllInventories();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping("/{inventoryId}/products/{productId}/restockProduct")
    public Mono<ResponseEntity<ProductResponseDTO>> restockLowStockProduct(
            @PathVariable String inventoryId,
            @PathVariable String productId,
            @RequestParam Integer productQuantity,
            ServerWebExchange exchange
    ) {
        if (productQuantity == null || productQuantity <= 0) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        return inventoryServiceClient.restockLowStockProduct(
                        inventoryId,
                        productId,
                        productQuantity,
                        currentUsername(exchange))
                .map(updatedProduct -> ResponseEntity.status(HttpStatus.OK).body(updatedProduct))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @GetMapping("/{inventoryId}/productquantity")
    public Mono<ResponseEntity<Integer>> getQuantityOfProductsInInventory(
            @PathVariable String inventoryId
    ) {
        return inventoryServiceClient.getQuantityOfProductsInInventory(inventoryId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PatchMapping("/{inventoryId}/important")
    public Mono<ResponseEntity<Object>> updateImportantStatus(
            @PathVariable String inventoryId,
            @RequestBody Map<String, Boolean> request
    ) {
        Boolean important = request.get("important");

        return inventoryServiceClient.updateImportantStatus(inventoryId, important)
                .then(Mono.just(ResponseEntity.ok().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {
            Roles.ADMIN, Roles.INVENTORY_MANAGER, Roles.VET
    })
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryResponseDTO> searchInventory(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(required = false) String inventoryCode,
            @RequestParam(required = false) String inventoryName,
            @RequestParam(required = false) String inventoryType,
            @RequestParam(required = false) String inventoryDescription,
            @RequestParam(required = false) Boolean importantOnly
    ) {
        return inventoryServiceClient.searchInventory(
                page.isEmpty() ? Optional.of(0) : page,
                size.isEmpty() ? Optional.of(10) : size,
                inventoryCode,
                inventoryName,
                inventoryType,
                inventoryDescription,
                importantOnly
        );
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
            @PathVariable String newInventoryId
    ) {
        return inventoryServiceClient
                .updateProductInventoryId(currentInventoryId, productId, newInventoryId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}