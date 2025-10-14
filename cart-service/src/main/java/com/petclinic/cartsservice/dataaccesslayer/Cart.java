package com.petclinic.cartsservice.dataaccesslayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import reactor.core.publisher.Flux;

import java.util.List;


@Document(collection = "cart")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    private String id;

    private List<CartProduct> products;
    private List<CartProduct> wishListProducts;
    private String cartId;
    private String customerId;
    //added those
    private double subtotal;
    private double tvq;
    private double tvc;
    private double total;
    private String invoiceId;
    // Add recent purchases field
    private List<CartProduct> recentPurchases;
    private List<CartProduct> recommendationPurchase;
}
