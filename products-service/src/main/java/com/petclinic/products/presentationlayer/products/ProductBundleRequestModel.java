package com.petclinic.products.presentationlayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBundleRequestModel {
    private String bundleName;
    private String bundleDescription;
    private List<String> productIds;
    private Double bundlePrice;
}
