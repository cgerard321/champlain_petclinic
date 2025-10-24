package com.petclinic.visits.visitsservicenew.Utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMM-dd");
    private static final int SUFFIX_WIDTH = 2;

    // Use AtomicInteger for safe concurrent increments
    private static final AtomicInteger latestVisitIdSuffix = new AtomicInteger(0);
    // volatile ensures visibility of date changes across threads
    private static volatile String latestVisitDate;

    private static final AtomicInteger latestReviewIdSuffix = new AtomicInteger(0);
    private static volatile String latestReviewDate;

    private static final String visitPrefix = "VIST";
    private static final String reviewPrefix = "REVIEW";

    // Locks to synchronize per-id-type operations
    private static final Object visitLock = new Object();
    private static final Object reviewLock = new Object();

    public static String generateVisitId() {
        String today = LocalDate.now().format(DATE_FORMAT);

        synchronized (visitLock) {
            // Initialize / reset if first call of the day
            if (latestVisitDate == null || !latestVisitDate.equals(today)) {
                latestVisitDate = today;
                latestVisitIdSuffix.set(0);
            }

            int suffix = latestVisitIdSuffix.getAndIncrement();
            return generateId(visitPrefix, today, suffix);
        }
    }

    public static String generateReviewId() {
        String today = LocalDate.now().format(DATE_FORMAT);

        synchronized (reviewLock) {
            // Initialize / reset if first call of the day
            if (latestReviewDate == null || !latestReviewDate.equals(today)) {
                latestReviewDate = today;
                latestReviewIdSuffix.set(0);
            }

            int suffix = latestReviewIdSuffix.getAndIncrement();
            return generateId(reviewPrefix, today, suffix);
        }
    }

    /**
     * Generate a readable id using the pattern PREFIX-yyMM-ddSS, where SS is a zero-padded sequence number that resets each day.
     * previousIdSuffix is the last stored suffix (0-based), so displayed SS = previousIdSuffix + 1.
     */
    public static String generateId(String prefix, String datePart, int previousIdSuffix) {

        String paddedSuffix = String.format("%0" + SUFFIX_WIDTH + "d", previousIdSuffix + 1);

        // This will format it in the format VIST-2510-2401
        // The 2 first are year, next 2 are month, next 2 is the day and last 2 is an increment
        return String.format("%s-%s%s", prefix, datePart, paddedSuffix);
    }

}
