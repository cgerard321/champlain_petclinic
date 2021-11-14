package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author mszarlinski@bravurasolutions.com on 2016-12-05.
 * @author lpsim
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 *
 */
@Data
class PetDetails {

    private long id;

    private String name;

    private String owner;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    private PetType type;

    public PetDetails()
    {

    }

    public PetDetails(Pet pet) {
        this.id = pet.getId();
        this.name = pet.getName();
        this.owner = pet.getOwner().getFirstName() + " " + pet.getOwner().getLastName();
        this.birthDate = pet.getBirthDate();
        this.type = pet.getType();
    }
}

