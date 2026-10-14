package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseModel {
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerAddress;
    private LocalDateTime orderDate;
    private List<CartProduct> products;
    private double subtotal;
    private Double promoPercent;
    private double tvq;
    private double tvc;
    private double total;

    private String invoiceId;



    public OrderResponseModel(String invoiceId, String orderId, List<CartProduct> products, double total) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.products = products;
        this.total = total;
    }
}
