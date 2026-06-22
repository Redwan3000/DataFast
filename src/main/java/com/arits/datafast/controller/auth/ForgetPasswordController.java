package com.arits.datafast.controller.auth;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.auth.AuthService;
import com.arits.datafast.state.AppState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class ForgetPasswordController {

    private final AuthService authService = new AuthService();

    @FXML private TextField emailField;
    @FXML private HBox      errorBanner;
    @FXML private Label     errorLabel;
    @FXML private Button    sendButton;

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();

        // 1. Empty check
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            return;
        }

        // 2. Format check — before hitting API
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        hideError();
        setFormEnabled(false);

        AppState.getInstance().setTempEmail(email);

        new Thread(() -> {
            long start = System.currentTimeMillis();
            try {
                authService.requestOtp(email);
                System.out.println("OTP request took: " + (System.currentTimeMillis() - start) + "ms");
                Platform.runLater(() -> SceneRouter.navigateTo("/auth/reset-password-view.fxml"));
            } catch (Exception e) {
                System.out.println("OTP request FAILED after: " + (System.currentTimeMillis() - start) + "ms");
                Platform.runLater(() -> { setFormEnabled(true); showError(e.getMessage()); });
            }
        }).start();
    }

    @FXML
    private void handleBackToLogin() {
        SceneRouter.navigateTo("/auth/login-view.fxml");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorBanner.setVisible(true);
            errorBanner.setManaged(true);
        });
    }

    private void hideError() {
        Platform.runLater(() -> {
            errorBanner.setVisible(false);
            errorBanner.setManaged(false);
        });
    }

    private void setFormEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        sendButton.setDisable(!enabled);
        sendButton.setText(enabled ? "Send Reset Code" : "Sending...");
    }
}