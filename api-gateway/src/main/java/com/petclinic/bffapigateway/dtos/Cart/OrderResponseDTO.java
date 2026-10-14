package com.petclinic.bffapigateway.dtos.Cart;

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
public class OrderResponseDTO {

    private String orderId;
    private String customerId;
    private String customerName;
    private String customerAddress;
    private LocalDateTime orderDate;

    private List<CartProductResponseDTO> products;

    private double subtotal;
    private Double promoPercent;
    private double tvq;
    private double tvc;
    private double total;

    private String invoiceId;
}