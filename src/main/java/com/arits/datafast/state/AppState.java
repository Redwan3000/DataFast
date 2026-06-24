package com.arits.datafast.state;

import com.arits.datafast.browser.BrowserManager;
import com.arits.datafast.dto.auth.UserSessionData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class AppState {

    private static volatile AppState appState;

    // --- Auth ---
    private volatile String authToken;
    private volatile String userJson;

    // users sensitive data encrypted
    private volatile String userName;
    private volatile String userEmail;
    private volatile String userPhone;

    //users non sensitive data
    private volatile int userId;
    private volatile String userRole;

    //Company non-sensitive
    private volatile int companyId;
    private volatile String companyName;
    private volatile String companyEmail;
    private volatile String companyAddress;


    private volatile boolean loggedIn = false;

    @Setter
    private volatile String tempEmail;
    @Setter
    private volatile BrowserManager.BrowserSession browserSession;


    public static AppState getAppState() {

        if (appState == null) {
            synchronized (AppState.class) {
                if (appState == null) {
                    appState = new AppState();
                }
            }
        }
        return appState;
    }


    public void setSession(String authToken, String userJson, UserSessionData data) {
        this.authToken = authToken;
        this.userJson = userJson;
        this.userId = data.userId();
        this.userName = data.userName();
        this.userEmail = data.userEmail();
        this.userPhone = data.userPhone();
        this.userRole = data.userRole();
        this.companyId = data.companyId();
        this.companyName = data.companyName();
        this.companyEmail = data.companyEmail();
        this.companyAddress = data.companyAddress();
        this.loggedIn = true;
    }


    public synchronized void closeBrowser() {
        if (browserSession != null) {
            try {
                browserSession.close();
            } catch (Exception e) {
                log.warn("[AppState] failed to close browser clearly", e);
            }
            browserSession = null;
        }
    }

    public synchronized void clearSession() {
        closeBrowser();
        authToken = null;
        userJson = null;
        userId = 0;
        userName = null;
        userEmail = null;
        userPhone = null;
        userRole = null;
        companyId = 0;
        companyName = null;
        companyEmail = null;
        companyAddress = null;
        loggedIn = false;
        tempEmail = null;
    }

    @Override
    public String toString() {
        return "AppState{"
                + "loggedIn=" + loggedIn
                + ", userId=" + userId
                + ", userRole=" + userRole
                + ", companyId=" + companyId
                + ", hasToken=" + (authToken != null)
                + ", hasBrowser=" + (browserSession != null)
                + "}";
    }
}