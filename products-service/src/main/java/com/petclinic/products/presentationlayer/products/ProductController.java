package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.businesslayer.products.ProductService;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseModel> getAllProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort) {
        return productService.getAllProducts(minPrice, maxPrice,sort);
    }

    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseModel>> getProductByProductId(@PathVariable String productId) {
        return Mono.just(productId)
                .filter(id -> id.length() == 36) // validate the product id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                .flatMap(productService::getProductByProductId)
                .map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/{productId}")
    public Mono<ResponseEntity<Void>> incrementRequestCount(@PathVariable String productId) {
        return productService.requestCount(productId).then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseModel>> addProduct(@RequestBody Mono<ProductRequestModel> productRequestModel) {
        return productService.addProduct(productRequestModel)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseModel>> updateProduct(@RequestBody Mono<ProductRequestModel> productRequestModel,
                                                                    @PathVariable String productId) {
        return Mono.just(productId)
                .filter(id -> id.length() == 36) // validate the product id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                .flatMap(id -> productService.updateProductByProductId(id, productRequestModel))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseModel>> deleteProduct(@PathVariable String productId) {
        return Mono.just(productId)
                .filter(id -> id.length() == 36) // validate the product id
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                .flatMap(productService::deleteProductByProductId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    @GetMapping(value = "/filter/{productType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductResponseModel> getProductsByType(@PathVariable String productType) {
        return productService.getProductsByType(productType);
    }

}
