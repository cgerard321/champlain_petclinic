package com.petclinic.vets.datalayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VetDTO
{


    @Column(name = "vet_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Length(min = 1, max = 6,groups = VetDTO.class)
    @NotNull
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

    @Column(name = "resume")
    private String resume;

    @Column(name = "workday")
    private String workday;

    @Column(name = "is_active")
    private Integer isActive;

    public Integer getIsActive() {
        return isActive;
    }

    public String getPostNumber()
    {
        String postNumber = getPhoneNumber().replaceAll("\\(\\d{3}\\)-\\d{3}-\\d{4}", "");
        return postNumber;
    }
    private Set<Specialty> specialties;
}
