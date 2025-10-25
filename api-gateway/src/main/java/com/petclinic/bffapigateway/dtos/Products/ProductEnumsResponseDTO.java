package com.petclinic.bffapigateway.dtos.Products;

import com.petclinic.bffapigateway.dtos.Products.ProductType;
import com.petclinic.bffapigateway.dtos.Products.ProductStatus;
import com.petclinic.bffapigateway.dtos.Products.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEnumsResponseDTO {
    private List<ProductType> productType;
    private List<ProductStatus> productStatus;
    private List<DeliveryType> deliveryType;
}