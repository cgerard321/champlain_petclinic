package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseModel {

    private String cartId;
    private String customerId;
    private List<ProductResponseModel> products;
}