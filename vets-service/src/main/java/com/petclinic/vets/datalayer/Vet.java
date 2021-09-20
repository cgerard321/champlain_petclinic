package com.petclinic.vets.datalayer;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.xml.bind.annotation.XmlElement;
import java.util.*;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Entity
@Table(name = "vets")
public class Vet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    @NotEmpty
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty
    private String lastName;

    //Fields added for new vet -C.D.
    @Column(name = "email")
    @NotEmpty
    private String email;

    @Column(name = "phone_number")
    @NotEmpty
    private String phoneNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    private Set<Specialty> specialties;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    //Fields added for new vet -C.D.
    public String getEmail() {return this.email;}

    public void setEmail(String email) {this.email = email;}

    public String getPhoneNumber() {return this.phoneNumber;}

    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}

    //Add a list of animals that a vet treats
//    @XmlElement
//    public List<AnimalTreated> getAnimalTypeTreated() {
//        List<Specialty> sortedSpecs = new ArrayList<>(getAnimalTypeTreated());
//        PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("animalType", true, true));
//        return Collections.unmodifiableList(sortedSpecs);
//    }
//    protected Set<AnimalTreated> setAnimalTypeTreatedInternal() {
//        if (this.animals == null) {
//            this.animals = new HashSet<>();
//        }
//        return this.animals;
//    }
//    public void addAnimalTreated(AnimalTreated animalTreated) {
//        getAnimalTypeTreated().add(animalTreated);
//    }
//
    //Add list of possible wor days for vets
//    @XmlElement
//    public List<WorkDays> getWorkDays() {
//        List<WorkDays> sortedSpecs = new ArrayList<>(getWorkDays());
//        PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("workDays", true, true));
//        return Collections.unmodifiableList(sortedSpecs);
//    }
//    protected Set<WorkDays> setWorkDaysInternal() {
//        if (this.workDays == null) {
//            this.workDays = new HashSet<>();
//        }
//        return this.workDays;
//    }
//    public void addWorkDays(WorkDays workDays) {
//        getWorkDays().add(workDays);
//    }

    protected Set<Specialty> getSpecialtiesInternal() {
        if (this.specialties == null) {
            this.specialties = new HashSet<>();
        }
        return this.specialties;
    }

    @XmlElement
    public List<Specialty> getSpecialties() {
        List<Specialty> sortedSpecs = new ArrayList<>(getSpecialtiesInternal());
        PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedSpecs);
    }

    public int getNrOfSpecialties() {
        return getSpecialtiesInternal().size();
    }

    public void addSpecialty(Specialty specialty) {
        getSpecialtiesInternal().add(specialty);
    }

}