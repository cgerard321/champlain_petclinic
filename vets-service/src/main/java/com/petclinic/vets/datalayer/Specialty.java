package com.petclinic.vets.datalayer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Models a {@link Vet Vet's} specialty (for example, dentistry).
 *
 * @author Juergen Hoeller
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * @Contributor Christian Chitanu
 */

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
    @UniqueElements(groups = Specialty.class)
    @Length(min = 6,max = 6, groups = Specialty.class)
    private Integer specialtyId;

    @Column(name = "name")
    private String name;

    public Integer getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Integer specialtyId) {
        this.specialtyId = specialtyId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = DataValidation.verifySpeciality(name);
    }
}
