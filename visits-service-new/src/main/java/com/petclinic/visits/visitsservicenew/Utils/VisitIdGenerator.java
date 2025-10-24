package com.petclinic.visits.visitsservicenew.Utils;

import java.util.concurrent.ThreadLocalRandom;

public class VisitIdGenerator {

    private static final String VISIT_PREFIX = "VIST-";
    private static final String REVIEW_PREFIX = "REVIEW-";

    public static String generateVisitId() {
        return generateId(VISIT_PREFIX);
    }

    public static String generateReviewId() {
        return generateId(REVIEW_PREFIX);
    }

    static String generateId(String prefix) {
        long timestamp = System.currentTimeMillis();
        int randomPart = ThreadLocalRandom.current().nextInt(0, 10_000);
        return prefix + timestamp + String.format("-%04d", randomPart);
    }
}
