package com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Role {
    private int id;
    private String name;
}
