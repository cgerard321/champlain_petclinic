package com.petclinic.cartsservice.domainclientlayer;

@Deprecated(forRemoval = true)
public class AddProductRequestModel extends CartItemRequestModel {

    public AddProductRequestModel() {
        super();
    }

    public AddProductRequestModel(String productId, Integer quantity) {
        super(productId, quantity);
    }
}
