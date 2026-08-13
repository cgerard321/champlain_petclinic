package com.petclinic.bffapigateway.dtos.Cart;

public enum WishlistTransferDirectionDTO {
    TO_CART,
    TO_WISHLIST;

    public static WishlistTransferDirectionDTO defaultDirection() {
        return TO_CART;
    }
}
