package com.petclinic.bffapigateway.dtos;
import lombok.*;
import java.util.Set;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetDTO {
    private String vetId;
    private String vetBillId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private byte[] image;
    private String resume;
    private String workday;
    private boolean isActive;
    private Set<SpecialtyDTO> specialties;


}
