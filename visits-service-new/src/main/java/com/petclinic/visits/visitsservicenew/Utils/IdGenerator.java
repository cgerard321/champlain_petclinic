package com.petclinic.visits.visitsservicenew.Utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMM-dd");
    // Use a region-based ZoneId to get correct DST behavior and align with other services
    private static final ZoneId TIMEZONE = ZoneId.of("America/Montreal");

    private static final String VISIT_PREFIX = "VIST";
    private static final String REVIEW_PREFIX = "REVIEW";
    private static final int MINIMAL_SUFFIX_LENGTH = 2;

    // State holders per id type
    private static final IdState VISIT_STATE = new IdState(VISIT_PREFIX);
    private static final IdState REVIEW_STATE = new IdState(REVIEW_PREFIX);

    // Generate a Visit ID like VIST-yyMM-ddSS
    public static String generateVisitId() {
        return VISIT_STATE.nextId();
    }

    // Generate a Review ID like REVIEW-yyMM-ddSS
    public static String generateReviewId() {
        return REVIEW_STATE.nextId();
    }

    private static final class IdState {
        private final String prefix;
        // Start from 1 and use postfix increment when emitting IDs
        private int suffix = 1;
        private int width = MINIMAL_SUFFIX_LENGTH;
        private volatile String latestDate;
        // This is to avoid race condition
        private final Object lock = new Object();

        private IdState(String prefix) {
            this.prefix = prefix;
        }

        private String nextId() {
            synchronized (lock) {
                // Setting a specific timezone to ensure consistent id generation
                String today = LocalDate.now(TIMEZONE).format(DATE_FORMAT);

                // Reset counter when date changes
                if (latestDate == null || !latestDate.equals(today)) {
                    latestDate = today;
                    suffix = 1; // restart sequence at 1 for a new day
                    width = MINIMAL_SUFFIX_LENGTH;
                }

                // Get current suffix then increment for next call
                int nextSuffix = suffix++;

                // Grow width if needed based on next suffix
                int requiredWidth = calculateRequiredWidth(nextSuffix);
                if (requiredWidth > width) {
                    width = requiredWidth;
                }

                return format(prefix, today, nextSuffix, width);
            }
        }
    }

    /**
     * Calculate the required width for a given (displayed) suffix value.
     * Ensures a minimum width of 2.
     */
    private static int calculateRequiredWidth(int displayedSuffix) {
        int digits = String.valueOf(displayedSuffix).length();
        return Math.max(MINIMAL_SUFFIX_LENGTH, digits);
    }

    /**
     * Format the id as PREFIX-yyMM-ddSSS...
     */
    private static String format(String prefix, String datePart, int displayedSuffix, int suffixWidth) {
        String paddedSuffix = String.format("%0" + suffixWidth + "d", displayedSuffix);
        return String.format("%s-%s%s", prefix, datePart, paddedSuffix);
    }
}
