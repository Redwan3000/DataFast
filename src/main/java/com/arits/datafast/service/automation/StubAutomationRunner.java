package com.arits.datafast.service.automation;

import com.arits.datafast.state.FieldMappingRow;

import java.io.File;
import java.util.List;

/**
 * Placeholder until the real Playwright engine exists. Swap this for the
 * real implementation in ReviewRunController — nothing else changes.
 */
public class StubAutomationRunner implements AutomationRunner {

    @Override
    public void run(File excelFile, List<FieldMappingRow> mapping, int startRow, AutomationLogSink sink) {
        new Thread(() -> {
            sink.log(AutomationLogSink.LogLevel.INFO, "Welcome to the Terminal!");
            sleep(400);
            sink.log(AutomationLogSink.LogLevel.WARN, "Waiting for user to log in manually...");
            sleep(2000);
            sink.log(AutomationLogSink.LogLevel.INFO, "Login detected. Starting from row " + startRow + "...");
            sleep(800);
            sink.log(AutomationLogSink.LogLevel.SUCCESS, "Row " + startRow + " submitted successfully.");
            sleep(600);
            sink.onFinished(true, "Stub run finished — no real submission happened.");
        }, "stub-automation-runner").start();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}