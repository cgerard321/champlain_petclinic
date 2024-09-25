package com.petclinic.visits.visitsservicenew.Exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DuplicateTimeExceptionTest {
    @Test
    public void testDuplicateReminderException() {
        String message = "Duplicate reminder found";
        DuplicateReminderException exception = new DuplicateReminderException(message);

        assertTrue(exception instanceof RuntimeException);
        assertEquals(message, exception.getMessage());
    }
}
