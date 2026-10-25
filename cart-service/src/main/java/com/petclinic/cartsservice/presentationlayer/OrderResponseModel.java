package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.ShippingMethod;
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
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String email;
    private LocalDateTime orderDate;
    private List<CartProduct> products;
    private double subtotal;
    private Double promoPercent;
    private double tvq;
    private double tvc;
    private double total;
    private ShippingMethod shippingMethod;
    private double shippingCost;

    private String invoiceId;


    public OrderResponseModel(
            String orderId,
            String customerId,
            String customerName,
            String customerAddress,
            LocalDateTime orderDate,
            List<CartProduct> products,
            double subtotal,
            Double promoPercent,
            double tvq,
            double tvc,
            double total,
            String invoiceId
    ) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.address = customerAddress;
        this.orderDate = orderDate;
        this.products = products;
        this.subtotal = subtotal;
        this.promoPercent = promoPercent;
        this.tvq = tvq;
        this.tvc = tvc;
        this.total = total;
        this.invoiceId = invoiceId;
    }


    public OrderResponseModel(String invoiceId, String orderId, List<CartProduct> products, double total) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.products = products;
        this.total = total;
    }
}
