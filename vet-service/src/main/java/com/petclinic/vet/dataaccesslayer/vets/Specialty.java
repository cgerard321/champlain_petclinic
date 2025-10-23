package com.petclinic.vet.dataaccesslayer.vets;
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
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Specialty {
    private String specialtyId;  // Will store UUID
    private String name;
    
    public Specialty(String name) {
        this.specialtyId = UUID.randomUUID().toString();
        this.name = name;
    }

}
