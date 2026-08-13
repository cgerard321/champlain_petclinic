package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.businesslayer.products.ProductBundleService;
import com.petclinic.products.businesslayer.products.ProductService;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.utils.EntityModelUtil;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductBundleService bundleService;
    private final ProductService productService;

    @Autowired
    public ProductController(ProductBundleService productBundleService, ProductService productService) {
        this.productService = productService;
        this.bundleService = productBundleService;
    }

    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductResponseModel> getAllProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String deliveryType,
            @RequestParam(required = false) String productType) {

        return productService.getAllProducts(minPrice, maxPrice, minRating, maxRating, sort,deliveryType,productType);
    }

    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseModel>> getProductByProductId(@PathVariable String productId) {
        return Mono.just(productId)
                .filter(id -> id.length() == 36)
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
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                .flatMap(id -> productService.updateProductByProductId(id, productRequestModel))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PatchMapping(value = "/{productId}/status")
    public Mono<ResponseEntity<ProductResponseModel>> patchListingStatus(@PathVariable String productId,
                                                    @RequestBody Mono<ProductRequestModel> productRequestModel) {
        return Mono.just(productId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                .flatMap(id -> productService.patchListingStatus(id, productRequestModel))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductResponseModel>> deleteProduct(@PathVariable String productId,
                                                                    @RequestParam(name = "cascadeBundles", defaultValue = "false") boolean cascadeBundles) {
        return Mono.just(productId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product id is invalid: " + productId)))
                .flatMap(foundId -> productService.deleteProductByProductId(foundId, cascadeBundles))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    @GetMapping(value = "/filter/{productType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductResponseModel> getProductsByType(@PathVariable String productType) {
        return productService.getProductsByType(productType);
    }
    @PatchMapping(value = "/{productId}/decrease")
    public Mono<ResponseEntity<Void>> decreaseProductQuantity(@PathVariable String productId) {
        return productService.DecreaseProductCount(productId).then(Mono.just(ResponseEntity.noContent().build()));
    }
    @PatchMapping(value = "/{productId}/quantity")
    public Mono<ResponseEntity<Void>> changeProductQuantity(@PathVariable String productId, @RequestBody Mono<ProductRequestModel> productRequestModel) {
        return productRequestModel
                .flatMap(request -> productService.changeProductQuantity(productId, request.getProductQuantity()))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
    @GetMapping("/filter")
    public List<ProductResponseModel> getProductsByType(@RequestParam ProductType productType) {
        List<Product> products = productService.getProductsByType(productType);
        // Convert the entities to response models (if needed)
        return products.stream()
                .map(EntityModelUtil::toProductResponseModel)
                .toList();
    }



    // New endpoints for product bundles
    @GetMapping(value = "/bundles", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductBundleResponseModel> getAllProductBundles() {
        return bundleService.getAllProductBundles();
    }
    @GetMapping(value = "/bundles/{bundleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductBundleResponseModel>> getProductBundleById(@PathVariable String bundleId) {
        return bundleService.getProductBundleById(bundleId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @PostMapping(value = "/bundles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductBundleResponseModel>> createProductBundle(@RequestBody Mono<ProductBundleRequestModel> requestModel) {
        return bundleService.createProductBundle(requestModel)
                .map(bundle -> ResponseEntity.status(HttpStatus.CREATED).body(bundle));
    }
    @PutMapping(value = "/bundles/{bundleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductBundleResponseModel>> updateProductBundle(@PathVariable String bundleId,
                                                                                @RequestBody Mono<ProductBundleRequestModel> requestModel) {
        return bundleService.updateProductBundle(bundleId, requestModel)
                .map(ResponseEntity::ok);
    }
    @DeleteMapping(value = "/bundles/{bundleId}")
    public Mono<ResponseEntity<Void>> deleteProductBundle(@PathVariable String bundleId) {
        return bundleService.deleteProductBundle(bundleId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping(value = "/enums")
    public Mono<ResponseEntity<ProductEnumsResponseModel>> getProductsEnumValues(){
        return productService.getProductsEnumValues()
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/types", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductTypeResponseModel> getAllProductTypes() {
        return productService.getAllProductTypes();
    }

    @GetMapping(value = "/types/{productTypeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductTypeResponseModel>> getProductTypeByProductTypeId(@PathVariable String productTypeId) {
        return Mono.just(productTypeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product type id is invalid: " + productTypeId)))
                .flatMap(productService::getProductTypeByProductTypeId)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/types", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductTypeResponseModel>> addProductType(@RequestBody Mono<ProductTypeRequestModel> productTypeRequestModel) {
        return productService.addProductType(productTypeRequestModel)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(value = "/types/{productTypeId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductTypeResponseModel>> updateProductType(@RequestBody Mono<ProductTypeRequestModel> productTypeRequestModel,
                                                                    @PathVariable String productTypeId) {
        return Mono.just(productTypeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product type id is invalid: " + productTypeId)))
                .flatMap(id -> productService.updateProductTypeByProductTypeId(id, productTypeRequestModel))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(value = "/types/{productTypeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductTypeResponseModel>> deleteProductType(@PathVariable String productTypeId) {
        return Mono.just(productTypeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided product type id is invalid: " + productTypeId)))
                .flatMap(productService::deleteProductTypeByProductTypeId)
                .map(ResponseEntity::ok);
    }
}
