package com.petclinic.visits.visitsservicenew.Exceptions;

/**
 * Will be called when trying to register a Reminder that is already created for an appointment
 */
public class DuplicateReminderException extends RuntimeException {
    public DuplicateReminderException(String message) {
        super(message);
    }
}
