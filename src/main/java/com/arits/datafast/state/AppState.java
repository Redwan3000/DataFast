package com.arits.datafast.state;

import com.arits.datafast.browser.BrowserManager;

/**
 * In-memory session state for the current logged-in user.
 *
 * Lifecycle:
 *   - Populated on successful login (token + userData from Laravel)
 *   - Held in memory for the session duration only — never persisted to disk
 *   - Cleared on logout or token expiry
 *   - Login is the natural cache-reset boundary: no manual invalidation needed
 */
public class AppState {

    private static volatile AppState instance;
    private volatile String authToken;
    private volatile String userJson;
    private volatile String userName;
    private volatile String userEmail;
    private volatile boolean loggedIn = false;
    private String tempEmail; // New field for password reset flow
    /** Live browser session — opened after login, closed on logout. */
    private volatile BrowserManager.BrowserSession browserSession;
    private AppState() {}

    public static AppState getInstance() {
        if (instance == null) {
            synchronized (AppState.class) {
                if (instance == null) {
                    instance = new AppState();
                }
            }
        }
        return instance;
    }

    public String getTempEmail() { return tempEmail; }

    public void setTempEmail(String email) { this.tempEmail = email; }


    // --- Setters ---

    public void setSession(String authToken, String userJson,
                           String userName, String userEmail) {
        this.authToken = authToken;
        this.userJson  = userJson;
        this.userName  = userName;
        this.userEmail = userEmail;
        this.loggedIn  = true;
    }

    public String getAuthToken()  { return authToken; }

    // --- Getters ---

    public String getUserJson()   { return userJson; }

    public String getUserName()   { return userName; }

    public String getUserEmail()  { return userEmail; }

    public boolean isLoggedIn()   { return loggedIn; }

    public BrowserManager.BrowserSession getBrowserSession() { return browserSession; }

    public void setBrowserSession(BrowserManager.BrowserSession browserSession) {
        this.browserSession = browserSession;
    }

    // --- Logout ---

    /**
     * Closes the browser session if open, then clears all session data.
     */
    public synchronized void clear() {
        if (browserSession != null) {
            try { browserSession.close(); } catch (Exception ignored) {}
            browserSession = null;
        }
        authToken = null;
        userJson  = null;
        userName  = null;
        userEmail = null;
        loggedIn  = false;
        tempEmail = null;
    }

    @Override
    public String toString() {
        return "AppState{loggedIn=" + loggedIn
                + ", userEmail=" + userEmail
                + ", hasToken=" + (authToken != null)
                + ", hasBrowser=" + (browserSession != null) + "}";
    }
}
