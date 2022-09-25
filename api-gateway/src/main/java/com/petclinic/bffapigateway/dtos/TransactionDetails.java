package com.petclinic.bffapigateway.dtos;

import lombok.Data;

@Data
public class TransactionDetails {
    private int billId;
    private int ownerId;

    public TransactionDetails(int billId, int ownerId) {
        billId = billId;
        ownerId = ownerId;
    }
}
