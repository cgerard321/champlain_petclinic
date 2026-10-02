package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.datalayer.products.DeliveryType;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.datalayer.products.ProductTypeDb;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEnumsResponseModel {
    private List<ProductTypeDb> productType;
    private List<ProductStatus> productStatus;
    private List<DeliveryType> deliveryType;
}