package com.petclinic.inventoryservice.datalayer.Product;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Product {
    @Id
    private String id;
    private String productId;
    private String inventoryId;
    private String productName;
    private String productDescription;
    private Integer productQuantity;
    private Double productPrice;
    private Double productSalePrice;
}
