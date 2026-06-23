package com.arits.datafast.state;

import java.io.File;

/**
 * Holds the in-flight state for one automation run session.
 *
 * Lifecycle:
 *   - Set by DashboardController when user clicks a sub-module
 *   - Read by AutomationRunnerController (Step 1), MapColumnsController (Step 2), etc.
 *   - Cleared when user returns to Dashboard or a new automation is started
 */
public class AutomationContext {

    private static volatile AutomationContext instance;

    private int    automationId;
    private String subModuleName;
    private String subModuleDescription;
    private int    moduleId;
    private String moduleName;

    // Populated after Step 1
    private File   selectedFile;

    private AutomationContext() {}

    public static AutomationContext getInstance() {
        if (instance == null) {
            synchronized (AutomationContext.class) {
                if (instance == null) {
                    instance = new AutomationContext();
                }
            }
        }
        return instance;
    }

    /** Called by DashboardController before navigating to the runner. */
    public void start(int moduleId, String moduleName,
                      int automationId, String subModuleName, String subModuleDescription) {
        this.moduleId             = moduleId;
        this.moduleName           = moduleName;
        this.automationId         = automationId;
        this.subModuleName        = subModuleName;
        this.subModuleDescription = subModuleDescription;
        this.selectedFile         = null;   // reset from any previous run
    }

    public void clear() {
        automationId         = 0;
        subModuleName        = null;
        subModuleDescription = null;
        moduleId             = 0;
        moduleName           = null;
        selectedFile         = null;
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public int    getAutomationId()          { return automationId; }
    public String getSubModuleName()         { return subModuleName; }
    public String getSubModuleDescription()  { return subModuleDescription; }
    public int    getModuleId()              { return moduleId; }
    public String getModuleName()            { return moduleName; }
    public File   getSelectedFile()          { return selectedFile; }
    public void   setSelectedFile(File f)    { this.selectedFile = f; }
}