package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
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
    private String customerName;

    private List<CartProduct> products;
    private double subtotal;
    private double tvq;
    private double tvc;
    private double total;

    private List<CartProduct> wishListProducts;
    private String message;
    private String paymentStatus;
    private String invoiceId;

    public CartResponseModel(String invoiceId, String cartId, List<CartProduct> products, double total) {
        this.invoiceId = invoiceId;
        this.cartId = cartId;
        this.products = products;
        this.total = total;
    }
}
