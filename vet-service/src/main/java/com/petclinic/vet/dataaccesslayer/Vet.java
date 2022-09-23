package com.petclinic.vet.dataaccesslayer;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Document(value="veterinarians")
public class Vet {

    @Id
    private String id;
    private String vetId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private byte[] image;
    private String resume;
    private String workday;
    private Integer isActive;
    private Set<Specialty> specialties;

}
