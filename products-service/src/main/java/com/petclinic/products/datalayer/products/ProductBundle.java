package com.petclinic.products.datalayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("product_bundles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBundle {
    @Id
    private long id;
    private String bundleId;
    private String bundleName;
    private String bundleDescription;
    private List<String> productIds;
    private Double originalTotalPrice;
    private Double bundlePrice;
}
