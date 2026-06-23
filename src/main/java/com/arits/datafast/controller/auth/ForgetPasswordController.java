package com.arits.datafast.controller.auth;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.auth.AuthService;
import com.arits.datafast.state.AppState;
import com.arits.datafast.util.helpers.ErrorHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ForgetPasswordController {

    private final AuthService authService = new AuthService();

    @FXML
    private TextField emailField;
    @FXML
    private HBox errorBanner;
    @FXML
    private Label errorLabel;
    @FXML
    private Button sendButton;

    private ErrorHelper errorHelper;

    @FXML
    public void initialize() {
        errorHelper = new ErrorHelper(errorBanner, errorLabel);
    }


    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();


        if (email.isEmpty()) {
            errorHelper.showError("Please enter your email address.");
            return;
        }

        if (!isValidEmail(email)) {
            errorHelper.showError("Please enter a valid email address.");
            return;
        }


        errorHelper.hideError();
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
                Platform.runLater(() -> {
                    setFormEnabled(true);
                    errorHelper.showError(e.getMessage());
                });
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


    private void setFormEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        sendButton.setDisable(!enabled);
        sendButton.setText(enabled ? "Send Reset Code" : "Sending...");
    }
}