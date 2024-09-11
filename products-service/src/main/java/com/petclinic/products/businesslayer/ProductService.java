package com.petclinic.products.businesslayer;

import com.petclinic.products.presentationlayer.ProductRequestModel;
import com.petclinic.products.presentationlayer.ProductResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Flux<ProductResponseModel> getAllProducts();
    Mono<ProductResponseModel> getProductByProductId(String productId);
    Mono<ProductResponseModel> addProduct(Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> updateProductByProductId(String productId, Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> deleteProductByProductId(String productId);
}
