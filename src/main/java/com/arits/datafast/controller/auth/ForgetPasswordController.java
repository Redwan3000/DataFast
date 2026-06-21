package com.arits.datafast.controller.auth;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.api.ApiClient;
import com.arits.datafast.service.auth.AuthService;
import com.arits.datafast.state.AppState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

// Add this method inside the class

public class ForgetPasswordController {

    @FXML private TextField emailField;
    private void showErrorDialog(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            System.out.println("Email field is empty!");
            return;
        }

        System.out.println("Initiating OTP request for: " + email);
        AppState.getInstance().setTempEmail(email);

        Thread thread = new Thread(() -> {
            try {
                AuthService authService = new AuthService();
                // Capture the response properly
                ApiClient.ApiResponse response = authService.requestOtp(email);

                Platform.runLater(() -> {
                    // Check the status code (ensure your ApiClient.ApiResponse class has a getCode() method)
                    if (response.getCode() == 200) {
                        SceneRouter.navigateTo("/auth/reset-password-view.fxml");
                    } else {
                        String msg = authService.getErrorMessage(response.getBody());
                        showErrorDialog(msg);
                    }
                });
            } catch (Exception e) {
                showErrorDialog("Connection error: " + e.getMessage());
            }
        });
        thread.start();
    }

    @FXML
    private void handleBackToLogin() {
        // Just navigate, no need to set tempEmail here
        SceneRouter.navigateTo("/auth/login-view.fxml");
    }
}