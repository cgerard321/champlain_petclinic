package com.petclinic.customers.datalayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.core.style.ToStringCreator;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import java.util.*;

/**
 * Simple JavaBean domain object representing an owner.
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * @author lpsim
 */

@Entity
@Table(name = "owners")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    @NotEmpty
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty
    private String lastName;

    @Column(name = "address")
    @NotEmpty
    private String address;

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Digits(fraction = 0, integer = 10)
    private String telephone;

    @Column(name = "image_id")
    private int imageId;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "owner")
    private Set<Pet> pets;



//    @Column(name = "custodian")
//    private String custodian;


    public Owner(@NotEmpty  Integer id, @NotEmpty String firstName, @NotEmpty String lastName,
                 @NotEmpty String address, @NotEmpty String city, @NotEmpty @Digits(fraction = 0, integer = 10) String telephone, @NotEmpty int imageId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.telephone = telephone;
        this.imageId = imageId;
    }


    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getImageId() {
        return this.imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    protected Set<Pet> getPetsInternal() {
        if (this.pets == null) {
            this.pets = new HashSet<>();
        }
        return this.pets;
    }

    public List<Pet> getPets() {
        final List<Pet> sortedPets = new ArrayList<>(getPetsInternal());
        PropertyComparator.sort(sortedPets, new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedPets);
    }

    public void addPet(Pet pet) {
        getPetsInternal().add(pet);
        pet.setOwner(this);
    }

    public void removePet(Pet pet)
    {
        getPetsInternal().remove(pet);
    }


    @Override
    public String toString()
    {
        String id_str = this.id.toString();
        String ownerString = "ID: " +
                id_str + ", First Name: " +
                this.firstName + ", Last Name: " +
                this.lastName + ", Address: " +
                this.address + ", City: " +
                this.city + ", Telephone: " +
                this.telephone;
        return ownerString;
    }

}

