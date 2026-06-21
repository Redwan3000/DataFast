package com.arits.datafast.controller.auth;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.auth.AuthService; // Ensure this exists
import com.arits.datafast.state.AppState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ResetPasswordController {

    private final AuthService authService = new AuthService();
    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void handleReset() {
        String otp = otpField.getText().trim();
        String password = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (!password.equals(confirm)) {
            System.err.println("Passwords do not match!");
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                // Get the email stored by the previous controller
                String email = AppState.getInstance().getTempEmail();

                if (email == null) throw new Exception("Session expired: Email missing.");

                authService.resetPassword(email, otp, password);

                // Success: clear temp data and return to login
                AppState.getInstance().setTempEmail(null);
                Platform.runLater(() -> SceneRouter.navigateTo("/auth/login-view.fxml"));

            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("Reset failed: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}