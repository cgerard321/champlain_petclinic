package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.PageRequest;

import java.util.Map;
import java.util.Optional;
@RestController
@AllArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductInventoryService productInventoryService;

//    @PostMapping("/{inventoryId}/products")
//    public Mono<ResponseEntity<ProductResponseDTO>> addProductToInventory(@RequestBody Mono<ProductRequestDTO> newProduct, @PathVariable String inventoryId){
//        return productInventoryService.addProductToInventory(newProduct, inventoryId)
//                .map(productResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(productResponseDTO))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }

    @DeleteMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProductToInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return productInventoryService.deleteProductInInventory(inventoryId, productId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
    @PostMapping()
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(@RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO){
        return productInventoryService.addInventory(inventoryRequestDTO)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }

    @GetMapping(value = "/{inventoryId}/products", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO>
    getProductsInInventoryByInventoryIdAndProductField(@PathVariable String inventoryId,
                                                       @RequestParam(required = false) String productName,
                                                       @RequestParam(required = false) Double minPrice,
                                                       @RequestParam(required = false) Double maxPrice,
                                                       @RequestParam(required = false) Integer productQuantity,
                                                       @RequestParam(required = false) Double minSalePrice,
                                                       @RequestParam(required = false) Double maxSalePrice

    ){
        return productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(inventoryId, productName, minPrice, maxPrice, productQuantity, minSalePrice, maxSalePrice);
    }

    @GetMapping(value = "/{inventoryId}/products-pagination",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getProductsInInventoryByInventoryIdAndProductFieldPagination(@PathVariable String inventoryId,
                                                                                                 @RequestParam(required = false) String productName,
                                                                                                 @RequestParam(required = false) Double minPrice,
                                                                                                 @RequestParam(required = false) Double maxPrice,
                                                                                                 @RequestParam(required = false) Integer productQuantity,
                                                                                                 @RequestParam(required = false) Double minSalePrice,
                                                                                                 @RequestParam(required = false) Double maxSalePrice,
                                                                                                 @RequestParam Optional<Integer> page,
                                                                                                 @RequestParam Optional<Integer> size){
        return productInventoryService.getProductsInInventoryByInventoryIdAndProductsFieldsPagination(inventoryId, productName, minPrice, maxPrice, productQuantity, minSalePrice, maxSalePrice, PageRequest.of(page.orElse(0),size.orElse(5)));
    }

    @GetMapping(value = "/{inventoryId}/products-count")
    public Mono<ResponseEntity<Long>> getTotalNumberOfProductsWithRequestParams(@PathVariable String inventoryId,
                                                                               @RequestParam(required = false) String productName,
                                                                               @RequestParam(required = false) Double minPrice,
                                                                               @RequestParam(required = false) Double maxPrice,
                                                                               @RequestParam(required = false) Integer productQuantity,
                                                                                @RequestParam(required = false) Double minSalePrice,
                                                                                @RequestParam(required = false) Double maxSalePrice){
        return productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(inventoryId, productName, minPrice, maxPrice, productQuantity, minSalePrice, maxSalePrice).count()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @GetMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>>getProductByProductIdInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return productInventoryService.getProductByProductIdInInventory(inventoryId,productId)
                .map(i -> ResponseEntity.status(HttpStatus.OK).body(i))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<InventoryResponseDTO> searchInventories(
        @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size,
        @RequestParam(name = "inventoryCode", required = false) String inventoryCode,
        @RequestParam(name = "inventoryName", required = false) String inventoryName,
        @RequestParam(name = "inventoryType", required = false) String inventoryType,
        @RequestParam(name = "inventoryDescription", required = false) String inventoryDescription,
        @RequestParam(name = "importantOnly", required = false) Boolean importantOnly) {

    return productInventoryService.searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)), inventoryCode,inventoryName, inventoryType, inventoryDescription, importantOnly);
}


    @GetMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(@PathVariable String inventoryId){
        return productInventoryService.getInventoryById(inventoryId)
                .map(i -> ResponseEntity.status(HttpStatus.OK).body(i))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }





    @PutMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory(@RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO, @PathVariable String inventoryId) {
        return productInventoryService.updateInventory(inventoryRequestDTO, inventoryId)
                .map(updatedStudent -> ResponseEntity.status(HttpStatus.OK).body(updatedStudent))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



    //delete all products and delete all inventory
    @DeleteMapping("/{inventoryId}/products")
    public Mono<ResponseEntity<Void>> deleteProductsForAnInventory(@PathVariable String inventoryId) {
        return productInventoryService.deleteAllProductsForAnInventory(inventoryId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping()
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return productInventoryService.deleteAllInventory()
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.BAD_REQUEST));
    }



    @PutMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(@RequestBody Mono<ProductRequestDTO> productRequestDTOMono,
                                                                             @PathVariable String inventoryId,
                                                                             @PathVariable String productId){
        return productInventoryService.updateProductInInventory(productRequestDTOMono, inventoryId, productId)
                .map(productResponseDTO -> ResponseEntity.ok().body(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{inventoryId}")
    public Mono<ResponseEntity<Void>> deleteInventoryByInventoryId(@PathVariable String inventoryId){
        return productInventoryService.deleteInventoryByInventoryId(inventoryId)
        .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping("/type")
    public Mono<ResponseEntity<InventoryTypeResponseDTO>> addInventoryType(@RequestBody Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO){
        return productInventoryService.addInventoryType(inventoryTypeRequestDTO)
                .map(inventoryTypeResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(inventoryTypeResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/type", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes(){
    return productInventoryService.getAllInventoryTypes();
    }

  
    @GetMapping("/{inventoryId}/products/lowstock")
    public Flux<ProductResponseDTO> getLowStockProducts(@PathVariable String inventoryId, @RequestParam Optional<Integer> threshold) {
        int stockThreshold = threshold.orElse(20);
        return productInventoryService.getLowStockProducts(inventoryId, stockThreshold);

    }

    @GetMapping(value = "/{inventoryId}/products/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<ProductResponseDTO>>> searchProducts(@PathVariable String inventoryId,
                                                                         @RequestParam(required = false) String productName,
                                                                         @RequestParam(required = false) String productDescription,
                                                                         @RequestParam(required = false) Status status) {
        Flux<ProductResponseDTO> products = productInventoryService.searchProducts(inventoryId, productName, productDescription, status);

        return products.hasElements()
                .flatMap(hasProducts -> {
                    if (hasProducts) {
                        return Mono.just(ResponseEntity.ok(products));
                    } else {
                        return Mono.just(ResponseEntity.noContent().build());
                    }
                });
    }

    @PostMapping("/{inventoryId}/products")
    public Mono<ResponseEntity<ProductResponseDTO>> addSupplyToInventory(@RequestBody Mono<ProductRequestDTO> newProduct, @PathVariable String inventoryId) {
        return productInventoryService.addSupplyToInventory(newProduct, inventoryId)
                .map(productResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{inventoryId}/productquantity")
    public Mono<Integer> getQuantityOfProductsInInventory (@PathVariable String inventoryId) {
        return productInventoryService.getQuantityOfProductsInInventory(inventoryId);
    }

    @PatchMapping("/{inventoryId}/products/{productId}/consume")
    public Mono<ResponseEntity<ProductResponseDTO>> consumeProduct(@PathVariable String inventoryId,
                                                                   @PathVariable String productId) {
        return productInventoryService.consumeProduct(inventoryId, productId)
                .map(productResponseDTO -> ResponseEntity.ok().body(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{inventoryId}/important")
    public Mono<ResponseEntity<Object>> updateImportantStatus(@PathVariable String inventoryId, @RequestBody Map<String, Boolean> request) {
        Boolean important = request.get("important");
        return productInventoryService.updateImportantStatus(inventoryId, important)
                .then(Mono.just(ResponseEntity.ok().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{inventoryId}/products/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<ByteArrayResource>> createSupplyPdf(@PathVariable String inventoryId) {
        return productInventoryService.createSupplyPdf(inventoryId)
                .map(pdfContent -> {
                    ByteArrayResource resource = new ByteArrayResource(pdfContent);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"supply_report_" + inventoryId + ".pdf\"");

                    return ResponseEntity.ok()
                            .contentLength(pdfContent.length)
                            .contentType(MediaType.APPLICATION_PDF)
                            .headers(headers)
                            .body(resource);

                });
    }

    @PutMapping("/{inventoryId}/products/{productId}/restockProduct")
    public Mono<ResponseEntity<ProductResponseDTO>> restockLowStockProduct(@PathVariable String inventoryId,@PathVariable String productId, @RequestParam Integer productQuantity){
        if (productQuantity == null || productQuantity <= 0) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }
        return productInventoryService.restockLowStockProduct(inventoryId, productId, productQuantity)
                .map(updatedProduct -> ResponseEntity.ok().body(updatedProduct))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{currentInventoryId}/products/{productId}/updateInventoryId/{newInventoryId}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInventoryId(@PathVariable String currentInventoryId,
                                                                             @PathVariable String productId,
                                                                             @PathVariable String newInventoryId){
        return productInventoryService.updateProductInventoryId(currentInventoryId, productId, newInventoryId)
                .map(productResponseDTO -> ResponseEntity.ok().body(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<InventoryResponseDTO> getAllInventories() {
        return productInventoryService.getAllInventories();
    }

}


