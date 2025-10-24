package com.petclinic.bffapigateway.presentationlayer.v1;


import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.*;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/gateway/products")
public class ProductControllerV1 {

    private final ProductsServiceClient productsServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value="", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseDTO> getAllProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String deliveryType,
            @RequestParam(required = false) String productType
    ){
        if ((minPrice != null && minPrice < 0) || (maxPrice != null && maxPrice < 0) ||
                (minRating != null && minRating < 0) || (maxRating != null && maxRating < 0)) {
            throw new IllegalArgumentException("Price and rating values cannot be negative");
        }

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return Flux.error(new IllegalArgumentException("minPrice cannot be greater than maxPrice"));
        }

        if (minRating != null && maxRating != null && minRating > maxRating) {
            return Flux.error(new IllegalArgumentException("minRating cannot be greater than maxRating"));
        }

        return productsServiceClient.getAllProducts(minPrice, maxPrice, minRating, maxRating, sort, deliveryType, productType);

    }


    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByProductId(@PathVariable String productId) {
        return productsServiceClient.getProductByProductId(productId)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseDTO>> addProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        return productsServiceClient.createProduct(productRequestDTO)
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping(value = "{productId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseDTO>> updateProduct(@PathVariable String productId,
                                                                  @RequestBody ProductRequestDTO productRequestDTO) {
        return productsServiceClient.updateProduct(productId, productRequestDTO)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @DeleteMapping(value = "/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> deleteProduct(@PathVariable String productId,
                                                    @RequestParam(name = "cascadeBundles", defaultValue = "false")  boolean cascadeBundles) {
        return productsServiceClient.deleteProduct(productId, cascadeBundles)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product));
    }


    //----------------------------------------------------------------------
    //TODO: Product bundle


    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/bundles", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductBundleResponseDTO> getAllProductBundles() {
        return productsServiceClient.getAllProductBundles();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/bundles/{bundleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductBundleResponseDTO>> getProductBundleById(@PathVariable String bundleId) {
        return productsServiceClient.getProductBundleById(bundleId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PostMapping(value = "/bundles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductBundleResponseDTO>> createProductBundle(@RequestBody ProductBundleRequestDTO requestDTO) {
        return productsServiceClient.createProductBundle(requestDTO)
                .map(bundle -> ResponseEntity.status(201).body(bundle));
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PutMapping(value = "/bundles/{bundleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductBundleResponseDTO>> updateProductBundle(@PathVariable String bundleId,
                                                                              @RequestBody ProductBundleRequestDTO requestDTO) {
        return productsServiceClient.updateProductBundle(bundleId, requestDTO)
                .map(ResponseEntity::ok);
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/bundles/{bundleId}")
    public Mono<ResponseEntity<Void>> deleteProductBundle(@PathVariable String bundleId) {
        return productsServiceClient.deleteProductBundle(bundleId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }


    //-----------------------------------------------------------------------
    //TODO: Product quantities

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @PatchMapping(value = "{productId}/decrease", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> decreaseProductQuantity(@PathVariable String productId) {
        return productsServiceClient.decreaseProductQuantity(productId).then(Mono.just(ResponseEntity.noContent().build()));
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PatchMapping(value = "{productId}/quantity", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> changeProductQuantity(
            @PathVariable String productId,
            @RequestBody Mono<ProductQuantityRequest> productQuantityRequest) {

        return productQuantityRequest
                .flatMap(request -> productsServiceClient.changeProductQuantity(productId, request.getProductQuantity()))
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(Exception.class, e -> {
                    // Log the error
                    log.error("Error changing product quantity", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }


    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @PatchMapping(value = "{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> incrementRequestCount(@PathVariable String productId) {
        return productsServiceClient.requestCount(productId).then(Mono.just(ResponseEntity.noContent().build()));
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.INVENTORY_MANAGER})
    @PatchMapping(value = "{productId}/status", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseDTO>> patchListingStatus(@PathVariable String productId,
                                                                       @RequestBody ProductRequestDTO productRequestDTO) {
        return productsServiceClient.patchListingStatus(productId, productRequestDTO)
                .map(product -> ResponseEntity.status(HttpStatus.OK).body(product))
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException.UnprocessableEntity) {
                        return Mono.just(ResponseEntity.unprocessableEntity().build());
                    }
                    else if (e instanceof WebClientResponseException.NotFound) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    else {
                        return Mono.error(e);
                    }
                });
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/enums", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ProductEnumsResponseDTO> getProductEnumsValues() {
        return productsServiceClient.getProductEnumsValues();
    }


}
