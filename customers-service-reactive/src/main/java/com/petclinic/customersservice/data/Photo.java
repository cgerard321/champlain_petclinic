package com.petclinic.customersservice.data;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Photo {

    @Id
    private int id;
    private String name;
    private String type;
    private byte[] photo;

}
