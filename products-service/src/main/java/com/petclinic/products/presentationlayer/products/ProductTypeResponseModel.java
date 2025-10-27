package com.petclinic.products.presentationlayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeResponseModel {
    private String productTypeId;
    private String typeName;
}
