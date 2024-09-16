package com.petclinic.inventoryservice.datalayer.Supply;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Supply {
    @Id
    private String id;
    private String supplyId;
    private String inventoryId;
    private String supplyName;
    private String supplyDescription;
    private Integer supplyQuantity;
    private Double supplyPrice;
    private Double supplySalePrice;

}

