package com.petclinic.products.datalayer.products;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;
    private String productId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private String productType;
    private Integer requestCount;
    private String productType;
}

