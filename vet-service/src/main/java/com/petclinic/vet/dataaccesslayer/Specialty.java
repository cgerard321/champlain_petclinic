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
import com.petclinic.vet.servicelayer.DataValidation;

import lombok.*;

import org.springframework.data.annotation.Id;


@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Specialty {
    @Id
    private Integer id;
    private Integer specialtyId;
    private String name;
    public void setName(final String name) {
        this.name = DataValidation.verifySpeciality(name);
    }

}
