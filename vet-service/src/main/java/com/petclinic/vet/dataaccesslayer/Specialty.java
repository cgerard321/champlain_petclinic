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
import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "specialties")
public class Specialty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "specialty_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    // **** CHECK WHAT THIS DOES LATER **** //
    //@UniqueElements(groups = Specialty.class)
    //@Length(min = 6,max = 6, groups = Specialty.class)
    private Integer specialtyId;

    @Column(name = "name")
    private String name;

    //used in testing
//    public void setName(final String name) {
//        this.name = DataValidation.verifySpeciality(name);
//    }


}
