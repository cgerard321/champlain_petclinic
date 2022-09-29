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

import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@ToString

public class Vet {

    @Id
    private Integer id;

    private Integer vetId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private byte[] image;
    private String resume;
    private String workday;
    private boolean isActive;
    private Set<Specialty> specialties;

    public Vet(Integer id, Integer vetId, String firstName, String lastName, String email, String phoneNumber, byte[] image, String resume, String workday, boolean isActive, Set<Specialty> specialties) {
        this.id = id;
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

    //    public void setVetId(int vetId) {
//        this.vetId = DataValidation.verifyVetId(vetId);
//    }
//    public void setEmail(String email) {
//        this.email = DataValidation.verifyEmail(email);
//    }
//    public void setPhoneNumber(String phoneNumber) {
//        this.phoneNumber = DataValidation.verifyPhoneNumber(phoneNumber);
//    }
//    public void setWorkday(String workday) {
//        this.workday = DataValidation.verifyWorkday(workday);
//    }
//    public void setFirstName(String firstName) {
//        this.firstName = DataValidation.verifyFirstName(firstName);
//    }
//    public void setLastName(String lastName) {
//        this.lastName = DataValidation.verifyLastName(lastName);
//    }

//    @Override
//    public String toString() {
//        return new ToStringCreator(this).append("id", this.getId())
//                .append("firstName", this.getFirstName())
//                .append("lastName", this.getLastName())
//                .append("email", this.getEmail())
//                .append("phoneNumber", this.getPhoneNumber())
//                .append("resume", this.getResume())
//                .append("workday", this.getWorkday()).toString();
//    }

}
