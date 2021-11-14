package com.petclinic.customers.datalayer;

import javax.persistence.*;

/**
 * @author Juergen Hoeller
 * Can be Cat, Dog, Hamster...
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Entity
@Table(name = "types")
public class PetType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    public PetType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public PetType(){

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
}

