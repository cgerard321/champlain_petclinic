package com.petclinic.visits.visitsservicenew.DomainClientLayer.Customers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDTO {
    private String customerId;
    private String firstName;
    private String lastName;
}