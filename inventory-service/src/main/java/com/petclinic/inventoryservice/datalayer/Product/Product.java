package com.petclinic.inventoryservice.datalayer.Product;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

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
    private String supplierName;
    private String productDescription;
    private Map<String, String> name;
    private Map<String, String> description;
    private Integer productQuantity;
    private Double productPrice;
    private Double productSalePrice;
    private Double productProfit;
    private Status status;
    private LocalDateTime lastUpdatedAt;
    private byte[] photoData;
    private String photoType;

    private LocalDate expirationDate;

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName == null ? null : supplierName.strip();
    }

}
