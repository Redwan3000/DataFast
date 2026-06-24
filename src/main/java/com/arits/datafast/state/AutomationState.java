package com.arits.datafast.state;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

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
    }

    public void clear() {
        automationId = 0;
        subModuleName = null;
        subModuleDescription = null;
        moduleId = 0;
        moduleName = null;
        selectedFile = null;
    }


}