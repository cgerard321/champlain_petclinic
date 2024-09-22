package com.petclinic.inventoryservice.datalayer.Supply;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Document(collection = "supplies")
public class Supply {

    @Id
    private String supplyId;
    private String inventoryId;
    private String supplyName;
    private String supplyDescription;
    private Integer supplyQuantity;
    private Double supplyPrice;
    private Double supplySalePrice;
    private Status status;
}

