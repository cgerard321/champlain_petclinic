package com.petclinic.cartsservice.businesslayer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CartQueryCriteria {
    Integer page;
    Integer size;
    String customerId;
    String customerName;
    Boolean assigned;

    public int resolvedPage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int resolvedSize(int defaultSize, int maxSize) {
        int effective = size == null ? defaultSize : size;
        if (effective <= 0) {
            return defaultSize;
        }
        return Math.min(effective, maxSize);
    }

    public String normalizedCustomerId() {
        return customerId == null ? null : customerId.trim();
    }

    public String normalizedCustomerName() {
        return customerName == null ? null : customerName.trim();
    }
}
