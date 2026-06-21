package com.arits.datafast.controller.auth;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.auth.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginController {

    private final AuthService authService = new AuthService();
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private FontIcon passwordToggleIcon;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handlePasswordToggle() {
        passwordVisible = !passwordVisible;
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);
        passwordToggleIcon.setIconLiteral(passwordVisible ? "gmi-visibility" : "gmi-visibility-off");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordVisibleField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        setFormEnabled(false);
        clearError();

        // Run network call on background thread so the UI doesn't freeze
        Thread loginThread = new Thread(() -> {
            try {
                // Controller just delegates to the Service
                authService.authenticate(email, password);

                // If no exception was thrown, we succeed and navigate
                Platform.runLater(() -> SceneRouter.navigateTo("/dashboard/dashboard-view.fxml"));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    setFormEnabled(true);
                });
            }
        }, "login-thread");

        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void showError(String message) {
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorLabel.setText(message);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    private void setFormEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
        passwordVisibleField.setDisable(!enabled);
        loginButton.setDisable(!enabled);
        loginButton.setText(enabled ? " Login" : " Authenticating...");
    }
    @FXML
    private void handleForgotPassword() {
        // This triggers the router to switch to your forgot password screen
        SceneRouter.navigateTo("/auth/forget-password-view.fxml");
    }
}