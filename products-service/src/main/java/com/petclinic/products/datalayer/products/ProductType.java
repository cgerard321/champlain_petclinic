package com.petclinic.products.datalayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products-types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductType {

    @Id
    private String id;
    private String typeId;
    private String typeName;
}
