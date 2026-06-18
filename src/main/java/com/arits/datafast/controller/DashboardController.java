package com.arits.datafast.controller;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.state.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for dashboard-view.fxml.
 *
 * Shown after successful login. Holds the logout button which closes
 * the browser session and returns to the login screen.
 */
public class DashboardController {

    @FXML private Label userLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        // Populate user info from AppState
        String name = AppState.getInstance().getUserName();
        userLabel.setText(name != null ? "Welcome, " + name : "");
    }

    @FXML
    private void handleLogout() {
        // AppState.clear() closes the browser session and wipes the token
        AppState.getInstance().clear();
        System.out.println("[DashboardController] Logged out. Returning to login.");

        // Navigate back to login screen
        SceneRouter.navigateTo("login-view.fxml");
    }
}
