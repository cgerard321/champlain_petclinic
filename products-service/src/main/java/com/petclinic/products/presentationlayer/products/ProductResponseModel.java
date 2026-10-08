package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.datalayer.products.DeliveryType;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.domainclientlayer.FileResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseModel {

    private String productId;
    private String imageId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private Integer requestCount;
    private Integer productQuantity;
    private Boolean isUnlisted;
    private String productType;
    private String productTypeId;
    //private ProductType productType;
    private LocalDate releaseDate;
    private ProductStatus productStatus;
    private DeliveryType deliveryType;
    private FileResponseDTO image;


}
