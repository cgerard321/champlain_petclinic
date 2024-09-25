package com.petclinic.products.businesslayer.products;

import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Flux<ProductResponseModel> getAllProducts(Double minPrice, Double maxPrice);
    Mono<ProductResponseModel> getProductByProductId(String productId);
    Mono<ProductResponseModel> addProduct(Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> updateProductByProductId(String productId, Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> deleteProductByProductId(String productId);
    Flux<ProductResponseModel> getProductsByType(String productType);

    Mono<Void> requestCount(String productId);
    Flux<ProductResponseModel> getProductsByType(String productType);

}
