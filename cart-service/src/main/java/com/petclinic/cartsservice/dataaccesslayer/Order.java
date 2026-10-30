package com.petclinic.cartsservice.dataaccesslayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    private String id;

    private List<CartProduct> products;
    private String orderId;
    private String customerId;
    private String customerName;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String email;
    private double subtotal;
    private Double promoPercent;
    private double tvq;
    private double tvc;
    private double total;
    private ShippingMethod shippingMethod;
    private double shippingCost;
    private String invoiceId;
    private LocalDateTime orderDate;

    //to remember which Stripe payment belongs to which order
    private String stripeSessionId;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidDate;
}
