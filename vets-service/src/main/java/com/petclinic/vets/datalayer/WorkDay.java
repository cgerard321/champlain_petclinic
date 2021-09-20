package com.petclinic.vets.datalayer;

import javax.persistence.*;

@Entity
@Table(name = "work_day")
public class WorkDay {

    //To Do add what workDays can be added and necessary fields to display
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
}
