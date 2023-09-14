package com.petclinic.inventoryservice.datalayer.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    @Id
    private String id;
    private String productId;
    private String inventoryId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Integer productQuantity;
}
