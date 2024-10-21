package com.petclinic.products.businesslayer.products;

import com.petclinic.products.datalayer.products.ProductTypeRepository;
import com.petclinic.products.presentationlayer.products.ProductTypeRequestModel;
import com.petclinic.products.presentationlayer.products.ProductTypeResponseModel;
import com.petclinic.products.utils.EntityModelUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductTypeServiceImpl implements ProductTypeService{

    private final ProductTypeRepository productTypeRepository;

    public ProductTypeServiceImpl(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }

    @Override
    public Flux<ProductTypeResponseModel> getAllProductTypes() {
        return productTypeRepository.findAll()
                .map(EntityModelUtil::toProductTypeResponseModel);
    }

    @Override
    public Mono<ProductTypeResponseModel> addProductType(Mono<ProductTypeRequestModel> productTypeRequestModel) {
        return productTypeRequestModel
                .map(EntityModelUtil::toProductTypeEntity)
                .flatMap(productTypeRepository::save)
                .map(EntityModelUtil::toProductTypeResponseModel);
    }
}
