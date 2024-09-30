package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.*;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/products")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80",  methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.DELETE})
public class ProductController {

    private final ProductsServiceClient productsServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getAllProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        // Validate negative prices
        if ((minPrice != null && minPrice < 0) || (maxPrice != null && maxPrice < 0)) {
            throw new IllegalArgumentException("Price values cannot be negative");
        }
        //error handling for if the minPrice is greater than maxPrice
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return Flux.error(new IllegalArgumentException("minPrice cannot be greater than maxPrice"));
        }
        return productsServiceClient.getAllProducts(minPrice, maxPrice);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByProductId(@PathVariable String productId) {
        return productsServiceClient.getProductByProductId(productId)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PatchMapping(value = "{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> incrementRequestCount(@PathVariable String productId) {
        return productsServiceClient.requestCount(productId).then(Mono.just(ResponseEntity.noContent().build()));
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseDTO>> addProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        return productsServiceClient.createProduct(productRequestDTO)
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping(value = "{productId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseDTO>> updateProduct(@PathVariable String productId,
                                                                   @RequestBody ProductRequestDTO productRequestDTO) {
        return productsServiceClient.updateProduct(productId, productRequestDTO)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "{productId}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String productId) {
        return productsServiceClient.deleteProduct(productId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping(value = "/filter/{productType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductResponseDTO> getProductsByType(@PathVariable String productType) {
        return productsServiceClient.getProductsByType(productType);
    }
}
