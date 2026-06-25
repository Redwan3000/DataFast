package com.arits.datafast.state;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
public class AutomationState {

    private static volatile AutomationState automationState;

    private int automationId;
    private String subModuleName;
    private String subModuleDescription;
    private int moduleId;
    private String moduleName;

    @Setter
    private File selectedFile;

    // -- Excel data (Step 1 -> Step 2) --
    @Setter
    private String sheetName;
    @Setter
    private List<String> excelHeaders;
    @Setter
    private List<List<String>> excelRows;

    // -- Field mapping (Step 2 -> Step 3) --
    @Setter
    private List<FieldMappingRow> fieldMappingRows;

    @Setter
    private int startRow = 1;

    public static AutomationState getAutomationState() {
        if (automationState == null) {
            synchronized (AutomationState.class) {
                if (automationState == null) {
                    automationState = new AutomationState();
                }
            }
        }
        return automationState;
    }

    public void start(int moduleId, String moduleName,
                      int automationId, String subModuleName, String subModuleDescription) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.automationId = automationId;
        this.subModuleName = subModuleName;
        this.subModuleDescription = subModuleDescription;
        this.selectedFile = null;
        this.sheetName = null;
        this.excelHeaders = null;
        this.excelRows = null;
        this.fieldMappingRows = null;
        this.startRow = 1;
    }

    public boolean allFieldsMapped() {
        return fieldMappingRows != null && !fieldMappingRows.isEmpty()
                && fieldMappingRows.stream().allMatch(FieldMappingRow::isMapped);
    }

    public void clear() {
        automationId = 0;
        subModuleName = null;
        subModuleDescription = null;
        moduleId = 0;
        moduleName = null;
        selectedFile = null;
        sheetName = null;
        excelHeaders = null;
        excelRows = null;
        fieldMappingRows = null;
        startRow = 1;
    }


    public void clearUploadedFileData() {
        this.selectedFile = null;
        this.sheetName = null;
        this.excelHeaders = null;
        this.excelRows = null;
        this.fieldMappingRows = null;
    }
}