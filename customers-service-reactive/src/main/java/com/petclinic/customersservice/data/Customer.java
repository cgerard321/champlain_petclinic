package com.petclinic.customersservice.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@Builder
@Getter
@AllArgsConstructor
public class Customer {

    @Id
    private String id;
    @Indexed(unique = true)
    private String customerId; // public id
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String province;
    private String telephone;
    private String photoId;
}
