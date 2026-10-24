package com.petclinic.bffapigateway.utils;

import java.util.Arrays;
import java.util.Optional;

public enum CanadianProvince {
    AB("Alberta"),
    BC("British Columbia"),
    MB("Manitoba"),
    NB("New Brunswick"),
    NL("Newfoundland and Labrador"),
    NS("Nova Scotia"),
    ON("Ontario"),
    PE("Prince Edward Island"),
    QC("Quebec"),
    SK("Saskatchewan"),
    NT("Northwest Territories"),
    NU("Nunavut"),
    YT("Yukon");

    private final String displayName;

    CanadianProvince(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<CanadianProvince> fromCode(String code) {
        if (code == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    public static boolean isValidName(String name) {
        return name != null && Arrays.stream(values())
                .anyMatch(p -> p.displayName.equalsIgnoreCase(name.trim()));
    }
}