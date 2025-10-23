package com.petclinic.bffapigateway.dtos.Cart;

import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private String cartId;
    private String customerId;
    private String customerName;
    private List<CartProductResponseDTO> products;
    private List<CartProductResponseDTO> wishListProducts;

    private double subtotal;
    private double tvq;
    private double tvc;
    private double total;
    private String message;  
    private String paymentStatus;
    private String invoiceId;

}