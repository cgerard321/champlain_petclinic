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
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.core.style.ToStringCreator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "veterinarians")
public class Vet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "vet_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UniqueElements(groups = Vet.class)
    private Integer vetId;

    @Column(name = "first_name")
    @NotEmpty
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty
    private String lastName;

    @Column(name = "email")
    @NotEmpty
    private String email;

    @Column(name = "phone_number")
    @NotEmpty
    private String phoneNumber;

    @Column(name = "image")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] image;

    @Column(name = "resume")
    private String resume;


    @Column(name = "workday")
    private String workday;

    @Column(name = "is_active")
    private boolean isActive;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"), inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    private Set<Specialty> specialties;


    public void setVetId(int vetId) {
        this.vetId = DataValidation.verifyVetId(vetId);
    }
    public void setEmail(String email) {
        this.email = DataValidation.verifyEmail(email);
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = DataValidation.verifyPhoneNumber(phoneNumber);
    }
    public void setWorkday(String workday) {
        this.workday = DataValidation.verifyWorkday(workday);
    }
    public void setFirstName(String firstName) {
        this.firstName = DataValidation.verifyFirstName(firstName);
    }
    public void setLastName(String lastName) {
        this.lastName = DataValidation.verifyLastName(lastName);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("id", this.getId())
                .append("firstName", this.getFirstName())
                .append("lastName", this.getLastName())
                .append("email", this.getEmail())
                .append("phoneNumber", this.getPhoneNumber())
                .append("resume", this.getResume())
                .append("workday", this.getWorkday()).toString();
    }

}
