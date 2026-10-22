package com.petclinic.visits.visitsservicenew.PresentationLayer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleRequestDTO {
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime visitDate;
}
