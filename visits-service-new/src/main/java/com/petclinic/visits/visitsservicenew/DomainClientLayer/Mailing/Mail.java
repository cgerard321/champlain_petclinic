package com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Mail {

    private String
            to,
            subject,
            message;
}
