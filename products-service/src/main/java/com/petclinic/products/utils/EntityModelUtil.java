package com.petclinic.products.utils;

import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.presentationlayer.products.ProductRequestModel;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.presentationlayer.ratings.RatingRequestModel;
import com.petclinic.products.presentationlayer.ratings.RatingResponseModel;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Mono;

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
                .averageRating(productRequestModel.getAverageRating())
                .productType(productRequestModel.getProductType())
                .build();
    }

    public static RatingResponseModel toRatingResponseModel(Rating rating){
        RatingResponseModel responseModel = new RatingResponseModel();
        BeanUtils.copyProperties(rating, responseModel);
        return responseModel;
    }

    public static Rating toRatingEntity(RatingRequestModel requestModel, String productId, String customerId){
        return Rating.builder()
                .productId(productId)
                .customerId(customerId)
                .rating(requestModel.getRating())
                .build();
    }

    public static String generateUUIDString() {
        return UUID.randomUUID().toString();
    }
}
