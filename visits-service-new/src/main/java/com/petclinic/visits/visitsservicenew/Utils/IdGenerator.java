package com.petclinic.visits.visitsservicenew.Utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static int latestVisitIdSuffix = 1;
    private static int latestReviewIdSuffix = 1;

    private static String visitPrefix = "VIST";
    private static String reviewPrefix = "REVIEW";

    public static String generateVisitId() {
        return generateId(visitPrefix, latestVisitIdSuffix);
    }

    public static String generateReviewId() {
        return generateId(reviewPrefix, latestReviewIdSuffix);
    }

    /**
     * Generate a readable id using the pattern PREFIX-yyMMdd-seq, where seq resets each day.
     * If previousId is null or malformed, sequence starts at 1.
     */
    public static String generateId(String prefix, int previousIdSuffix) {

        String datePart = LocalDate.now().format(DATE_FORMAT);
        int nextIdSuffix = previousIdSuffix + 1;

        return String.format("%s-%s-%d", prefix, datePart, nextIdSuffix);
    }

}
