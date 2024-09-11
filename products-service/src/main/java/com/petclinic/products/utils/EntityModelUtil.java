package com.petclinic.products.utils;

import com.petclinic.products.datalayer.Product;
import com.petclinic.products.presentationlayer.ProductRequestModel;
import com.petclinic.products.presentationlayer.ProductResponseModel;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

public class EntityModelUtil {

    public static ProductResponseModel toProductResponseModel(Product product) {
        ProductResponseModel productResponseModel = new ProductResponseModel();
        BeanUtils.copyProperties(product, productResponseModel);
        return productResponseModel;
    }

    public static Product toProductEntity(ProductRequestModel productRequestModel) {
        return Product.builder()
                .productId(generateUUIDString())
                .productName(productRequestModel.getProductName())
                .productDescription(productRequestModel.getProductDescription())
                .productSalePrice(productRequestModel.getProductSalePrice())
                .build();
    }

    public static String generateUUIDString() {
        return UUID.randomUUID().toString();
    }
}
