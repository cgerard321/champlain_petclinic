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
    private List<ProductResponseDTO> products;
}