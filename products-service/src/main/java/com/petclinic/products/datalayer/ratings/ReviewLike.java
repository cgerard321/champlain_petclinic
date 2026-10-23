package com.petclinic.products.datalayer.ratings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("review_likes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLike {
    @Id
    private long id;
    private String productId;
    private String reviewCustomerId;
    private String likingCustomerId;
    private LocalDateTime createdAt;
}
