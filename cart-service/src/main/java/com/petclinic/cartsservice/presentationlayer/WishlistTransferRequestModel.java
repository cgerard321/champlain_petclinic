package com.petclinic.cartsservice.presentationlayer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistTransferRequestModel {

    private List<String> productIds;

    public List<String> normalizedProductIds() {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        return productIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
