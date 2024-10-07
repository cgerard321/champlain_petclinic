package com.petclinic.products.businesslayer.products;

import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Flux<ProductResponseModel> getAllProducts(Double minPrice, Double maxPrice,Double minRating, Double maxRating, String sort);
    Mono<ProductResponseModel> getProductByProductId(String productId);
    Mono<ProductResponseModel> addProduct(Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> updateProductByProductId(String productId, Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> deleteProductByProductId(String productId);
    Mono<Void> requestCount(String productId);
    Mono<Void> DecreaseProductCount(String productId);//When item is sold in cart//temporarily in cart.
    Mono<Void> changeProductQuantity(String productId, Integer productQuantity);
    Flux<ProductResponseModel> getProductsByType(String productType);
}
