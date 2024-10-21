package com.petclinic.products.businesslayer.products;

import com.petclinic.products.presentationlayer.products.ProductTypeRequestModel;
import com.petclinic.products.presentationlayer.products.ProductTypeResponseModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductTypeService {

    Flux<ProductTypeResponseModel> getAllProductTypes();
    Mono<ProductTypeResponseModel> addProductType(Mono<ProductTypeRequestModel> productTypeRequestModel);
}
