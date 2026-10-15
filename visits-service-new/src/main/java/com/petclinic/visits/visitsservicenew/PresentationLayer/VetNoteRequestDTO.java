package com.petclinic.visits.visitsservicenew.PresentationLayer;

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
