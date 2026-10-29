package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditEntry;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final ProductInventoryService productInventoryService;

    @GetMapping("/{inventoryId}/products/{productId}/history")
    public Flux<SupplyAuditEntry> getSupplyHistory(
            @PathVariable String inventoryId,
            @PathVariable String productId
    ) {
        return productInventoryService.getSupplyHistory(inventoryId, productId);
    }

    @DeleteMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProductToInventory(
            @PathVariable String inventoryId,
            @PathVariable String productId,
            @RequestHeader("X-Actor-Username") String changedBy
    ) {
        return productInventoryService.deleteProductInInventory(inventoryId, productId, changedBy)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(
            @RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO
    ) {
        return productInventoryService.addInventory(inventoryRequestDTO)
                .map(inventory -> ResponseEntity.status(HttpStatus.CREATED).body(inventory))
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }

    @GetMapping(value = "/{inventoryId}/products", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductField(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer productQuantity,
            @RequestParam(required = false) Double minSalePrice,
            @RequestParam(required = false) Double maxSalePrice
    ) {
        return productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(
                inventoryId,
                productName,
                minPrice,
                maxPrice,
                productQuantity,
                minSalePrice,
                maxSalePrice
        );
    }

    @GetMapping(value = "/{inventoryId}/products-pagination", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
        return productInventoryService.getProductsInInventoryByInventoryIdAndProductsFieldsPagination(
                inventoryId,
                productName,
                minPrice,
                maxPrice,
                productQuantity,
                minSalePrice,
                maxSalePrice,
                PageRequest.of(page.orElse(0), size.orElse(5))
        );
    }

    @GetMapping("/{inventoryId}/products-count")
    public Mono<ResponseEntity<Long>> getTotalNumberOfProductsWithRequestParams(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer productQuantity,
            @RequestParam(required = false) Double minSalePrice,
            @RequestParam(required = false) Double maxSalePrice
    ) {
        return productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(
                        inventoryId,
                        productName,
                        minPrice,
                        maxPrice,
                        productQuantity,
                        minSalePrice,
                        maxSalePrice
                )
                .count()
                .map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    @GetMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByProductIdInInventory(
            @PathVariable String inventoryId,
            @PathVariable String productId
    ) {
        return productInventoryService.getProductByProductIdInInventory(inventoryId, productId)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryResponseDTO> searchInventories(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(name = "inventoryCode", required = false) String inventoryCode,
            @RequestParam(name = "inventoryName", required = false) String inventoryName,
            @RequestParam(name = "inventoryType", required = false) String inventoryType,
            @RequestParam(name = "inventoryDescription", required = false) String inventoryDescription,
            @RequestParam(name = "importantOnly", required = false) Boolean importantOnly
    ) {
        return productInventoryService.searchInventories(
                PageRequest.of(page.orElse(0), size.orElse(10)),
                inventoryCode,
                inventoryName,
                inventoryType,
                inventoryDescription,
                importantOnly
        );
    }

    @GetMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(
            @PathVariable String inventoryId
    ) {
        return productInventoryService.getInventoryById(inventoryId)
                .map(inventory -> ResponseEntity.status(HttpStatus.OK).body(inventory))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory(
            @RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO,
            @PathVariable String inventoryId
    ) {
        return productInventoryService.updateInventory(inventoryRequestDTO, inventoryId)
                .map(inventory -> ResponseEntity.status(HttpStatus.OK).body(inventory))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{inventoryId}/products")
    public Mono<ResponseEntity<Void>> deleteProductsForAnInventory(
            @PathVariable String inventoryId
    ) {
        return productInventoryService.deleteAllProductsForAnInventory(inventoryId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return productInventoryService.deleteAllInventory()
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(
            @RequestBody Mono<ProductRequestDTO> productRequestDTOMono,
            @PathVariable String inventoryId,
            @PathVariable String productId,
            @RequestHeader("X-Actor-Username") String changedBy
    ) {
        return productInventoryService.updateProductInInventory(
                        productRequestDTOMono,
                        inventoryId,
                        productId,
                        changedBy
                )
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{inventoryId}")
    public Mono<ResponseEntity<Void>> deleteInventoryByInventoryId(
            @PathVariable String inventoryId
    ) {
        return productInventoryService.deleteInventoryByInventoryId(inventoryId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping("/type")
    public Mono<ResponseEntity<InventoryTypeResponseDTO>> addInventoryType(
            @RequestBody Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO
    ) {
        return productInventoryService.addInventoryType(inventoryTypeRequestDTO)
                .map(inventoryType -> ResponseEntity.status(HttpStatus.CREATED).body(inventoryType))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/type", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes() {
        return productInventoryService.getAllInventoryTypes();
    }

    @GetMapping(value = "/{inventoryId}/products/lowstock", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getLowStockProducts(
            @PathVariable String inventoryId,
            @RequestParam Optional<Integer> threshold
    ) {
        return productInventoryService.getLowStockProducts(inventoryId, threshold.orElse(20));
    }

    @GetMapping(value = "/{inventoryId}/products/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<ProductResponseDTO>>> searchProducts(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productDescription,
            @RequestParam(required = false) Status status
    ) {
        Flux<ProductResponseDTO> products =
                productInventoryService.searchProducts(inventoryId, productName, productDescription, status);

        return products.hasElements()
                .flatMap(hasProducts -> hasProducts
                        ? Mono.just(ResponseEntity.ok(products))
                        : Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping("/{inventoryId}/products")
    public Mono<ResponseEntity<ProductResponseDTO>> addSupplyToInventory(
            @RequestBody Mono<ProductRequestDTO> newProduct,
            @PathVariable String inventoryId,
            @RequestHeader("X-Actor-Username") String changedBy
    ) {
        return productInventoryService.addSupplyToInventory(newProduct, inventoryId, changedBy)
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{inventoryId}/productquantity")
    public Mono<Integer> getQuantityOfProductsInInventory(
            @PathVariable String inventoryId
    ) {
        return productInventoryService.getQuantityOfProductsInInventory(inventoryId);
    }

    @PatchMapping("/{inventoryId}/products/{productId}/consume")
    public Mono<ResponseEntity<ProductResponseDTO>> consumeProduct(
            @PathVariable String inventoryId,
            @PathVariable String productId,
            @RequestHeader("X-Actor-Username") String changedBy
    ) {
        return productInventoryService.consumeProduct(inventoryId, productId, changedBy)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{inventoryId}/important")
    public Mono<ResponseEntity<Object>> updateImportantStatus(
            @PathVariable String inventoryId,
            @RequestBody Map<String, Boolean> request
    ) {
        Boolean important = request.get("important");

        return productInventoryService.updateImportantStatus(inventoryId, important)
                .then(Mono.just(ResponseEntity.ok().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{inventoryId}/products/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<ByteArrayResource>> createSupplyPdf(
            @PathVariable String inventoryId,
            @RequestParam(required = false) String lang
    ) {
        return (lang == null ? productInventoryService.createSupplyPdf(inventoryId)
                : productInventoryService.createSupplyPdf(inventoryId, lang))
                .map(pdfContent -> {
                    ByteArrayResource resource = new ByteArrayResource(pdfContent);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"supply_report_" + inventoryId + ".pdf\""
                    );

                    return ResponseEntity.ok()
                            .contentLength(pdfContent.length)
                            .contentType(MediaType.APPLICATION_PDF)
                            .headers(headers)
                            .body(resource);
                });
    }

    @PutMapping("/{inventoryId}/products/{productId}/restockProduct")
    public Mono<ResponseEntity<ProductResponseDTO>> restockLowStockProduct(
            @PathVariable String inventoryId,
            @PathVariable String productId,
            @RequestParam Integer productQuantity,
            @RequestHeader("X-Actor-Username") String changedBy
    ) {
        if (productQuantity == null || productQuantity <= 0) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        return productInventoryService
                .restockLowStockProduct(inventoryId, productId, productQuantity, changedBy)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{currentInventoryId}/products/{productId}/updateInventoryId/{newInventoryId}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInventoryId(
            @PathVariable String currentInventoryId,
            @PathVariable String productId,
            @PathVariable String newInventoryId
    ) {
        return productInventoryService
                .updateProductInventoryId(currentInventoryId, productId, newInventoryId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryResponseDTO> getAllInventories() {
        return productInventoryService.getAllInventories();
    }
}
