package com.petclinic.bffapigateway.dtos.Products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBundleResponseDTO {
    private String bundleId;
    private String bundleName;
    private String bundleDescription;
    private List<String> productIds;
    private Double originalTotalPrice;
    private Double bundlePrice;
}
