package com.petclinic.bffapigateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
@AllArgsConstructor(staticName="create")
public class TransactionDetails {
    private BillDetails billDetails;
    private OwnerDetails ownerDetails;
}
