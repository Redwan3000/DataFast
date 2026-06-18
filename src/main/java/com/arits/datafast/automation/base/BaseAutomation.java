package com.arits.datafast.automation.base;

import com.microsoft.playwright.Page;

/**
 * Contract that every automation class must fulfil.
 *
 * Two-method shape (mirrors the original project's route.ts pattern):
 *   prepare()  — validate inputs, parse any Excel/config data, throw early if invalid
 *   execute()  — run the actual Playwright automation, report progress
 *
 * Caller flow:
 *   automation.prepare();   // throws AutomationException on bad input
 *   automation.execute();   // throws AutomationException on runtime failure
 */
public interface BaseAutomation {

    /**
     * Validate inputs and prepare for execution.
     * Called before execute() — if this throws, execute() must not be called.
     *
     * @throws AutomationException if inputs are invalid or preparation fails
     */
    void prepare() throws AutomationException;

    /**
     * Run the automation using the given Playwright page.
     * The page is already navigated to the correct starting URL by the caller.
     *
     * @param page live Playwright page from BrowserManager.BrowserSession
     * @throws AutomationException if the automation fails at runtime
     */
    void execute(Page page) throws AutomationException;
}
