package com.petclinic.products.businesslayer.products;

import com.petclinic.products.presentationlayer.products.ProductBundleRequestModel;
import com.petclinic.products.presentationlayer.products.ProductBundleResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ProductBundleService {
    Flux<ProductBundleResponseModel> getAllProductBundles();
    Mono<ProductBundleResponseModel> getProductBundleById(String bundleId);
    Mono<ProductBundleResponseModel> createProductBundle(Mono<ProductBundleRequestModel> requestModel);
    Mono<ProductBundleResponseModel> updateProductBundle(String bundleId, Mono<ProductBundleRequestModel> requestModel);
    Mono<Void> deleteProductBundle(String bundleId);
    Flux<ProductBundleResponseModel> deleteAllProductBundlesByProductId(String productId);
}
