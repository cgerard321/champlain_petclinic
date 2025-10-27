package com.petclinic.customersservice.data;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@Builder
@Getter
@AllArgsConstructor
public class Owner {

    @Id
    private String id;
    private String ownerId; // public id
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String province;
    private String telephone;
    private String photoId;
}
