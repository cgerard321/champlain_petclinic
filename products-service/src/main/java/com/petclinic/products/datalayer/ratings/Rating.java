package com.petclinic.products.datalayer.ratings;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_ratings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @Id
    private String id;

    private String productId;

    private String customerId;

    private Byte rating;
}