package com.petclinic.products.datalayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;


@Document(collection = "product-types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeDb {

    @Id
    private String id;
    private String productTypeId;
    private String typeName;
}

