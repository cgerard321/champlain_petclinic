package com.petclinic.customers.presentationlayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.petclinic.customers.datalayer.PetType;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author mszarlinski@bravurasolutions.com on 2016-12-05.
 * @author lpsim
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Data
public class PetRequest {
    private int id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    @Size(min = 1)
    private String name;

    private PetType type;

    //USED FOR TESTING PURPOSE
    public PetRequest()
    {

    }

    public PetRequest(String name, Date birthDate, PetType type) {
        this.name = name;
        this.birthDate = birthDate;
        this.type = type;
    }
}

