package com.petclinic.vet.servicelayer;
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

@Data
@ToString
@Builder
@NoArgsConstructor
public class VetDTO {
    private Integer vetId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private byte[] image;
    private String resume;
    private String workday;
    private boolean isActive;
    private Set<SpecialtyDTO> specialties;

    public VetDTO(Integer vetId, String firstName, String lastName, String email, String phoneNumber, byte[] image, String resume, String workday, boolean isActive, Set<SpecialtyDTO> specialties) {
        this.vetId = vetId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.image = image;
        this.resume = resume;
        this.workday = workday;
        this.isActive = isActive;
        this.specialties = specialties;
    }
}
