package com.petclinic.vet.dataaccesslayer;
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

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Document(value="veterinarians")
public class Specialty {

    @Id
    private Integer id; //don't know if this is needed because we have one in vet class...
    private Integer specialtyId;
    private String name;


}
