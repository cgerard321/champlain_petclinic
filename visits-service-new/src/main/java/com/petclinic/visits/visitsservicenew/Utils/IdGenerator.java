package com.petclinic.visits.visitsservicenew.Utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMM-dd");
    private static final int SUFFIX_WIDTH = 2;

    private static int latestVisitIdSuffix = 0;
    private static String latestVisitDate;
    private static int latestReviewIdSuffix = 0;
    private static String latestReviewDate;

    private static String visitPrefix = "VIST";
    private static String reviewPrefix = "REVIEW";

    public static String generateVisitId() {
        String today = LocalDate.now().format(DATE_FORMAT);

        // Initialize / reset if first call of the day
        if (latestVisitDate == null || !latestVisitDate.equals(today)) {
            latestVisitDate = today;
            latestVisitIdSuffix = 0;
        }

        String id = generateId(visitPrefix, latestVisitIdSuffix);
        latestVisitIdSuffix++; // advance suffix for next generated id
        return id;
    }

    public static String generateReviewId() {
        String today = LocalDate.now().format(DATE_FORMAT);

        // Initialize / reset if first call of the day
        if (latestReviewDate == null || !latestReviewDate.equals(today)) {
            latestReviewDate = today;
            latestReviewIdSuffix = 0;
        }

        String id = generateId(reviewPrefix, latestReviewIdSuffix);
        latestReviewIdSuffix++; // advance suffix for next generated id
        return id;
    }

    /**
     * Generate a readable id using the pattern PREFIX-yyMM-ddSS, where SS is a zero-padded sequence number that resets each day.
     * previousIdSuffix is the last stored suffix (0-based), so displayed SS = previousIdSuffix + 1.
     */
    public static String generateId(String prefix, int previousIdSuffix) {

        String datePart = LocalDate.now().format(DATE_FORMAT);

        String paddedSuffix = String.format("%0" + SUFFIX_WIDTH + "d", previousIdSuffix + 1);

        // This will format it in the format VIST-2510-2401
        // The 2 first are year, next 2 are month, next 2 is the day and last 2 is an increment
        return String.format("%s-%s%s", prefix, datePart, paddedSuffix);
    }

}
