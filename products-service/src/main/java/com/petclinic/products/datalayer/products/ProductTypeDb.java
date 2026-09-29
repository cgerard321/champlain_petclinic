package com.petclinic.products.datalayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("product_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeDb {
    @Id
    private long id;
    private String productTypeId;
    private String typeName;
}

