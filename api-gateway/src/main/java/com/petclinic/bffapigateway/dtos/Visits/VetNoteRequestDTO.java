package com.petclinic.bffapigateway.dtos.Visits;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetNoteRequestDTO {
    private String vetNote;
}
