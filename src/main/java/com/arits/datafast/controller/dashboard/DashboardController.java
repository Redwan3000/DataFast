package com.arits.datafast.controller.dashboard;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.state.AppState;
import com.arits.datafast.util.CryptoUtil; // 1. IMPORT YOUR CRYPTO TOOL
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label userLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        // 1. Grab the ENCRYPTED string from your session state
        String encryptedName = AppState.getInstance().getUserName();

        if (encryptedName != null && !encryptedName.isBlank()) {
            try {
                // 2. Unlock the name back into plain text
                String realName = CryptoUtil.decryptAES(encryptedName);
                userLabel.setText("Welcome, " + realName);
            } catch (Exception e) {
                // Fallback if decryption fails (e.g. key mismatch or corrupted state)
                System.err.println("[Dashboard] Failed to decrypt user name: " + e.getMessage());
                userLabel.setText("Welcome, User");
            }
        } else {
            userLabel.setText("");
        }
    }

    @FXML
    private void handleLogout() {
        // AppState.clear() closes the browser session and wipes the token
        AppState.getInstance().clear();
        System.out.println("[DashboardController] Logged out. Returning to login.");

        // Navigate back to login screen
        SceneRouter.navigateTo("/auth/login-view.fxml");
    }
}