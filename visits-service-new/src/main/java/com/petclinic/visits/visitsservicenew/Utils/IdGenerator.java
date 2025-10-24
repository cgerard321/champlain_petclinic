package com.petclinic.visits.visitsservicenew.Utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int SUFFIX_WIDTH = 2;

    private static int latestVisitIdSuffix = 0;
    private static String latestVisitDate;
    private static int latestReviewIdSuffix = 0;
    private static String latestReviewDate;

    private static String visitPrefix = "VIST";
    private static String reviewPrefix = "REVIEW";

    public static String generateVisitId() {
        int suffix;
        String today = LocalDate.now().format(DATE_FORMAT);
        // Reset the suffix if day changed since last visit
        if (!latestVisitDate.equals(today)) {
            suffix = 0;
            latestVisitDate = today;
        } else {
            suffix = latestVisitIdSuffix;
        }
        return generateId(visitPrefix, suffix);
    }

    public static String generateReviewId() {
        int suffix;
        String today = LocalDate.now().format(DATE_FORMAT);
        // Reset the suffix if day changed since last review
        if (!latestReviewDate.equals(today)) {
            suffix = 0;
            latestReviewDate = today;
        } else {
            suffix = latestReviewIdSuffix;
        }
        return generateId(reviewPrefix, suffix);
    }

    /**
     * Generate a readable id using the pattern PREFIX-yyMMdd-seq, where seq resets each day.
     * If previousId is null or malformed, sequence starts at 1.
     */
    public static String generateId(String prefix, int previousIdSuffix) {

        String datePart = LocalDate.now().format(DATE_FORMAT);

        String paddedSuffix = String.format("%0" + SUFFIX_WIDTH + "d", previousIdSuffix + 1);

        return String.format("%s-%s-%s", prefix, datePart, paddedSuffix);
    }

}
