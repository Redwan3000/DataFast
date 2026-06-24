package com.arits.datafast.controller.auth;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.auth.AuthService;
import com.arits.datafast.util.helpers.ErrorHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginController {

    private final AuthService authService = new AuthService();
    @FXML private TextField emailField;
    @FXML private PasswordField passwordHiddenField;
    @FXML private TextField passwordVisibleField;
    @FXML private FontIcon passwordToggleIcon;
    @FXML private Button loginButton;
    @FXML private HBox errorBanner;
    @FXML private Label errorLabel;
    private boolean passwordVisible = false;
private ErrorHelper errorHelper;

    @FXML
    public void initialize() {
        errorHelper= new ErrorHelper(errorBanner, errorLabel);
        passwordVisibleField.textProperty().bindBidirectional(passwordHiddenField.textProperty());
    }

    @FXML
    private void handlePasswordToggle() {
        passwordVisible = !passwordVisible;
        passwordHiddenField.setVisible(!passwordVisible);
        passwordHiddenField.setManaged(!passwordVisible);
        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);
        passwordToggleIcon.setIconLiteral(passwordVisible ? "gmi-visibility" : "gmi-visibility-off");
    }

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordHiddenField.getText();

        if (email.isEmpty()) {
            errorHelper.showError("Please enter your email address.");
            return;
        }
        if (!isValidEmail(email)) {
            errorHelper.showError("Please enter a valid email address.");
            return;
        }
        if (password.isEmpty()) {
            errorHelper.showError("Please enter your password.");
            return;
        }

        errorHelper.hideError();

        new Thread(() -> {
            Platform.runLater(() -> setFormEnabled(false));
            try {
                authService.authenticateUser(email, password);
                Platform.runLater(() -> SceneRouter.navigateTo("/dashboard/dashboard-view.fxml"));
            } catch (Exception e) {
                Platform.runLater(() -> setFormEnabled(true));
                errorHelper.showError(e.getMessage());
            }
        }).start();
    }


    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    private void setFormEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        passwordHiddenField.setDisable(!enabled);
        passwordVisibleField.setDisable(!enabled);
        loginButton.setDisable(!enabled);
        loginButton.setText(enabled ? " Login" : " Authenticating...");
    }

//    method to redirect forget password page
    @FXML
    private void handleForgotPassword() {
        SceneRouter.navigateTo("/auth/forget-password-view.fxml");
    }
}