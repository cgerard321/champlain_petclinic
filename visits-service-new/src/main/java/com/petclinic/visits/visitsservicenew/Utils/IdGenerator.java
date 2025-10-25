package com.petclinic.visits.visitsservicenew.Utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMM-dd");

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
        private final AtomicInteger suffix = new AtomicInteger(0);
        private final AtomicInteger width = new AtomicInteger(MINIMAL_SUFFIX_LENGTH);
        private String latestDate;
        // This is to avoid race condition
        private final Object lock = new Object();

        private IdState(String prefix) {
            this.prefix = prefix;
        }

        private String nextId() {
            String today = LocalDate.now().format(DATE_FORMAT);
            synchronized (lock) {
                // Reset counter when date changes
                if (latestDate == null || !latestDate.equals(today)) {
                    latestDate = today;
                    suffix.set(0);
                    width.set(MINIMAL_SUFFIX_LENGTH);
                }

                // Start sequences at 1 for each day
                int nextSuffix = suffix.incrementAndGet();

                // Grow width if needed based on next suffix
                int requiredWidth = calculateRequiredWidth(nextSuffix);
                if (requiredWidth > width.get()) {
                    width.set(requiredWidth);
                }

                return format(prefix, today, nextSuffix, width.get());
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
