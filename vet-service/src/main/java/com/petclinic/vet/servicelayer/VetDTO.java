package com.petclinic.vet.servicelayer;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 202
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import com.petclinic.vet.dataaccesslayer.Specialty;
import lombok.*;

import java.util.Set;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VetDTO {
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
