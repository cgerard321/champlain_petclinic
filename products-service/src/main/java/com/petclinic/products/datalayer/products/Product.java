package com.petclinic.products.datalayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;
    private String productId;
    private String imageId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private Integer requestCount;
    private Integer productQuantity;
    private ProductType productType;
}

