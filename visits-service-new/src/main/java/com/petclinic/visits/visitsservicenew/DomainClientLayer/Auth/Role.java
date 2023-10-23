package com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Role {

    private int id;
    private String name;
}
