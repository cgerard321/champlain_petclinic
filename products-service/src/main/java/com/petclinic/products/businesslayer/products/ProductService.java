package com.petclinic.products.businesslayer.products;

import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductService {

    Flux<ProductResponseModel> getAllProducts(Double minPrice, Double maxPrice,Double minRating, Double maxRating, String sort,String deliveryType,String productType);
    Mono<ProductResponseModel> getProductByProductId(String productId);
    Mono<ProductResponseModel> addProduct(Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> updateProductByProductId(String productId, Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> patchListingStatus(String productId, Mono<ProductRequestModel> productRequestModel);
    Mono<ProductResponseModel> deleteProductByProductId(String productId, boolean cascadeBundle);
    Mono<Void> requestCount(String productId);
    Mono<Void> DecreaseProductCount(String productId);//When item is sold in cart//temporarily in cart.
    Mono<Void> changeProductQuantity(String productId, Integer productQuantity);
    Flux<ProductResponseModel> getProductsByType(String productType);
    List<Product> getProductsByType(ProductType productType);

}
