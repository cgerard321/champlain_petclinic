package com.petclinic.billing.datalayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Photo {

    @Id
    private String id;
    private String name;
    private String type;
    private String photo;

}
