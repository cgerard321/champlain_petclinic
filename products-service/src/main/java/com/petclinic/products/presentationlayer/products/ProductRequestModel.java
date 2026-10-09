package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.datalayer.products.DeliveryType;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.domainclientlayer.FileRequestDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestModel {
    private String productName;
    private String productDescription;
    private String productNameFr;
    private String productDescriptionFr;
    private Double productSalePrice;
    private Integer productQuantity;
    private Boolean isUnlisted;
    private String productTypeId;
    //private ProductType productType;
    private LocalDate releaseDate;
    private ProductStatus productStatus;
    private DeliveryType deliveryType;
    @Valid
    private FileRequestDTO image;
}
