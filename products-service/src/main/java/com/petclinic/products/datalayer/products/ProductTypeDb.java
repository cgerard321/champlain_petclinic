package com.petclinic.products.datalayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

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

