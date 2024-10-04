package com.petclinic.cartsservice.domainclientlayer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProductRequestModel {

    private String productId;
    private int quantity;

}
