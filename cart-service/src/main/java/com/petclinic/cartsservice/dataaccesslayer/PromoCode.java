package com.petclinic.cartsservice.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "promoCode")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoCode {
    @Id
    private String id;

    private String Name;

    private String code;

    private  double discount;

    private LocalDateTime  expirationDate;

    private  boolean isActive;

}
