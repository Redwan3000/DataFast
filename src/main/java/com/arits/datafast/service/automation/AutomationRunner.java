package com.arits.datafast.service.automation;

import com.arits.datafast.state.FieldMappingRow;

import java.io.File;
import java.util.List;

/**
 * Contract for the engine that actually drives the browser. Nothing implements this for real yet.
 */
public interface AutomationRunner {
    void run(File excelFile, List<FieldMappingRow> mapping, int startRow, AutomationLogSink sink);
}