package com.petclinic.cartsservice.presentationlayer;

public enum WishlistTransferDirection {
    TO_CART,
    TO_WISHLIST;

    public static WishlistTransferDirection defaultDirection() {
        return TO_CART;
    }
}
