package com.petclinic.products.datalayer.ratings;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("product_ratings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @Id
    private long id;
    private String productId;
    private String customerId;
    private Byte rating;
    private String review;
    private LocalDateTime createdAt;
}