package com.arits.datafast.service.automation;

/**
 * Implemented by whatever's displaying the run — currently ReviewRunController.
 */
public interface AutomationLogSink {
    void log(LogLevel level, String message);

    void onFinished(boolean success, String summary);

    enum LogLevel {INFO, SUCCESS, WARN, ERROR}
}