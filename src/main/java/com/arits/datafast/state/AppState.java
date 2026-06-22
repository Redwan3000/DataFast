package com.arits.datafast.state;

import com.arits.datafast.browser.BrowserManager;
import com.arits.datafast.dto.auth.UserSessionData;

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

    // --- Auth ---
    private volatile String authToken;
    private volatile String userJson;

    // --- User (sensitive — stored encrypted) ---
    private volatile String userName;
    private volatile String userEmail;
    private volatile String userPhone;

    // --- User (non-sensitive — stored plain) ---
    private volatile int    userId;
    private volatile String userRole;

    // --- Company (non-sensitive — stored plain) ---
    private volatile int    companyId;
    private volatile String companyName;
    private volatile String companyEmail;
    private volatile String companyAddress;

    // --- Session flags ---
    private volatile boolean loggedIn = false;

    // --- Password reset flow ---
    private volatile String tempEmail;

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

    // --- Session setter ---
    public void setSession(String authToken, String userJson, UserSessionData data) {
        this.authToken      = authToken;
        this.userJson       = userJson;
        this.userId         = data.userId();
        this.userName       = data.userName();
        this.userEmail      = data.userEmail();
        this.userPhone      = data.userPhone();
        this.userRole       = data.userRole();
        this.companyId      = data.companyId();
        this.companyName    = data.companyName();
        this.companyEmail   = data.companyEmail();
        this.companyAddress = data.companyAddress();
        this.loggedIn       = true;
    }

    // --- Getters ---
    public String  getAuthToken()      { return authToken; }
    public String  getUserJson()       { return userJson; }
    public int     getUserId()         { return userId; }
    public String  getUserName()       { return userName; }
    public String  getUserEmail()      { return userEmail; }
    public String  getUserPhone()      { return userPhone; }
    public String  getUserRole()       { return userRole; }
    public int     getCompanyId()      { return companyId; }
    public String  getCompanyName()    { return companyName; }
    public String  getCompanyEmail()   { return companyEmail; }
    public String  getCompanyAddress() { return companyAddress; }
    public boolean isLoggedIn()        { return loggedIn; }
    public boolean isAdmin()           { return "admin".equalsIgnoreCase(userRole); }

    // --- Temp email (password reset flow) ---
    public String getTempEmail()             { return tempEmail; }
    public void   setTempEmail(String email) { this.tempEmail = email; }

    // --- Browser session ---
    public BrowserManager.BrowserSession getBrowserSession() { return browserSession; }
    public void setBrowserSession(BrowserManager.BrowserSession session) {
        this.browserSession = session;
    }

    // --- Logout ---
    /**
     * Closes the browser session if open, then wipes all session data.
     */
    public synchronized void clear() {
        if (browserSession != null) {
            try { browserSession.close(); } catch (Exception ignored) {}
            browserSession = null;
        }
        authToken      = null;
        userJson       = null;
        userId         = 0;
        userName       = null;
        userEmail      = null;
        userPhone      = null;
        userRole       = null;
        companyId      = 0;
        companyName    = null;
        companyEmail   = null;
        companyAddress = null;
        loggedIn       = false;
        tempEmail      = null;
    }

    @Override
    public String toString() {
        return "AppState{"
                + "loggedIn="     + loggedIn
                + ", userId="     + userId
                + ", userRole="   + userRole
                + ", companyId="  + companyId
                + ", hasToken="   + (authToken != null)
                + ", hasBrowser=" + (browserSession != null)
                + "}";
    }
}