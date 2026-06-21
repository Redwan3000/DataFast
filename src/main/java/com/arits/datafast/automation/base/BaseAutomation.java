package com.arits.datafast.automation.base;

import com.microsoft.playwright.Page;

public interface BaseAutomation {


    void prepare() throws AutomationException;

    void execute(Page page) throws AutomationException;
}
