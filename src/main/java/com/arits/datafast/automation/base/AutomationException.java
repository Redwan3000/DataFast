package com.arits.datafast.automation.base;

/**
 * Thrown when an automation fails — either during prepare() or execute().
 * Carries a user-friendly message suitable for display in the UI.
 */
public class AutomationException extends Exception {

    public AutomationException(String message) {
        super(message);
    }

    public AutomationException(String message, Throwable cause) {
        super(message, cause);
    }
}
