package com.petclinic.visits.visitsservicenew.DomainClientLayer;
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

import java.util.Set;

/**
 * The vetDTO for the Vet-Service
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetDTO {
    private String vetId;
    private String vetBillId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String imageId;
    private String resume;
    private Set<Workday> workday;
    private String workHoursJson;
    private boolean active;
    private Set<SpecialtyDTO> specialties;


}