package com.petclinic.customersservice.util;

import java.util.UUID;

public class ValidationUtils {

    private ValidationUtils() {

    }

    public static boolean isValidUUID(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
