package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.datalayer.products.DeliveryType;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.datalayer.products.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeRequestModel {
    private String typeName;
}
