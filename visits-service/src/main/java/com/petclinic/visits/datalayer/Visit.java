package com.petclinic.visits.datalayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Indexed;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

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

@Data
@Entity
@Table(name = "visits")
@Builder(builderMethodName = "visit")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    @Column(name = "visit_id",unique = true, nullable = false)
    @Builder.Default
    private UUID visitId = UUID.randomUUID();

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

}
