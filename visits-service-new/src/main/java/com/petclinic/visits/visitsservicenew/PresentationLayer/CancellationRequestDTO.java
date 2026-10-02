package com.petclinic.visits.visitsservicenew.PresentationLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.CancellationReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationRequestDTO {
    private CancellationReason cancellationReason;
    private String cancellationReasonDetails;
}
