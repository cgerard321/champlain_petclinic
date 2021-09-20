package com.petclinic.vets.datalayer;

import javax.persistence.*;

@Entity
@Table(name = "animal_treated")
public class AnimalTreated {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //To Do add what animals can be treated and necessary fields
}
