package com.petclinic.bffapigateway.dtos.Cart;

@Deprecated(forRemoval = true)
public class AddProductRequestDTO extends CartItemRequestDTO {

    public AddProductRequestDTO() {
        super();
    }

    public AddProductRequestDTO(String productId, Integer quantity) {
        super(productId, quantity);
    }
}
