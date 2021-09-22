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

    @NotEmpty
    @UniqueElements
    @Column(name = "vet_id")
    private Integer vetId;

    @Column(name = "first_name")
    @NotEmpty
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty
    private String lastName;

    //Fields added for new vet -C.D.
    @Column(name = "enable")
    @NotEmpty
    private boolean enable;

    @Column(name = "email")
    @NotEmpty
    private String email;

    @Column(name = "phone_number")
    @NotEmpty
    private String phoneNumber;

    @Column(name = "resume")
    @NotEmpty
    private String resume;

    @Column(name = "workday")
    private String workday;

    public Integer getVetId() {
        return vetId;
    }

    public void setVetId(Integer vetId) {
        this.vetId = vetId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getWorkday() {
        return workday;
    }

    public void setWorkday(String workday) {
        this.workday = workday;
    }


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

    public boolean getEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }


    //Add a list of animals that a vet treats
//    private Set<AnimalTreated> animals;
//
//    @XmlElement
//    public List<AnimalTreated> getAnimalTypeTreated() {
//        List<AnimalTreated> sortedSpecs = new ArrayList<>(getAnimalTypeTreated());
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
//    private Set<WorkDay> workDays;
//    //Add list of possible work days for vets
//    @XmlElement
//    public List<WorkDay> getWorkDays() {
//        List<WorkDay> sortedSpecs = new ArrayList<>(getWorkDays());
//        PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("workDays", true, true));
//        return Collections.unmodifiableList(sortedSpecs);
//    }
//    protected Set<WorkDay> setWorkDaysInternal() {
//        if (this.workDays == null) {
//            this.workDays = new HashSet<>();
//        }
//        return this.workDays;
//    }
//    public void addWorkDay(WorkDay workDay) {
//        getWorkDays().add(workDay);
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