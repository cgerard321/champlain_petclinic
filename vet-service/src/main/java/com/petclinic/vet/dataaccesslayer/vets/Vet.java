package com.petclinic.vet.dataaccesslayer.vets;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Vet {

    @Id
    private String id;

    private String vetId;
    private String vetBillId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String imageId;
    private String resume;
    private String photoId; 
    private Set<Workday> workday;
    private boolean active;
    private Set<Specialty> specialties;
    private String workHoursJson;
}
