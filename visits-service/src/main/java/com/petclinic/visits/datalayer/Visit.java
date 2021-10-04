package com.petclinic.visits.datalayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Simple JavaBean domain object representing a visit.
 *
 * @author Ken Krebs
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

/*
 * This class defines the visit entity which matches the entity stored in the database.
 *
 * Contributors:
 */

@Entity
@Table(name = "visits")
@Builder(builderMethodName = "visit")
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Builder.Default
    @Column(name = "visit_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date = new Date();

    @Size(max = 8192)
    @Column(name = "description")
    private String description;

    @Column(name = "pet_id")
    private int petId;
    
    @Column(name = "practitioner_id")
    private int practitionerId;

    @Column(name = "status")
    private boolean status;



    //Constructor of the visit object
    public Visit(int id, Date date, String description, int petId){
        this.id = id;
        this.date = date;
        this.description = description;
        this.petId = petId;
    }

    /*
    Getter and setters for all values
     */

    public Integer getId() {
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getPetId() {
        return petId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setPetId(final int petId) {
        this.petId = petId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getPractitionerId() {
        return practitionerId;
    }
    
    public void setPractitionerId(int practitionerId) {
        this.practitionerId = practitionerId;
    }
}
