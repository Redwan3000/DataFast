package com.arits.datafast.controller;

// 1. IMPORTING YOUR SEPARATE AUTOMATION CLASS HERE
import com.arits.datafast.automation.YoutubeAutomation;
import com.arits.datafast.browser.BrowserManager;
import com.arits.datafast.network.ApiClient;
import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.state.AppState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * UI controller for login-view.fxml.
 * Responsibilities: capture input, validate, delegate to services, update view.
 * The heavy browser interaction logic has been offloaded to YoutubeAutomation.
 */
public class LoginController {

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
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        setFormEnabled(false);
        clearError();

        Thread loginThread = new Thread(() -> {
            try {

                String jsonBody = "{\"email\":\"" + email + "\","
                        + "\"password\":\"" + password + "\"}";

                ApiClient.ApiResponse response = ApiClient.post("/api/login", jsonBody);

                System.out.println("[LoginController] Status: " + response.getStatusCode());
                System.out.println("[LoginController] Body:   " + response.getBody());

                if (response.isSuccess()) {
                    String body  = response.getBody();
                    String token = extractJsonString(body, "token");
                    String name  = extractNestedJsonString(body, "userData", "name");

                    if (token == null || token.isBlank()) {
                        Platform.runLater(() -> {
                            showError("Login failed: unexpected response format.");
                            setFormEnabled(true);
                        });
                        return;
                    }

                    AppState.getInstance().setSession(token, body, name, email);
                    System.out.println("[LoginController] Session: " + AppState.getInstance());


                    BrowserManager.BrowserSession session =
                            BrowserManager.launch("https://www.youtube.com");
                    AppState.getInstance().setBrowserSession(session);


                    YoutubeAutomation automation = new YoutubeAutomation("Despacito");

                    automation.prepare();

                    automation.execute(session.getPage());

                    Platform.runLater(() -> SceneRouter.navigateTo("dashboard-view.fxml"));

                } else {
                    String serverMsg = extractJsonString(response.getBody(), "message");
                    String errorMsg  = serverMsg != null ? serverMsg
                            : response.getStatusCode() == 401
                              ? "Invalid email or password."
                              : "Login failed (status " + response.getStatusCode() + ").";

                    Platform.runLater(() -> {
                        showError(errorMsg);
                        setFormEnabled(true);
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error: " + e.getMessage());
                    setFormEnabled(true);
                });
            }
        }, "login-thread");

        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void showError(String message) { errorLabel.setText(message); }
    private void clearError()              { errorLabel.setText(""); }

    private void setFormEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
        loginButton.setDisable(!enabled);
        loginButton.setText(enabled ? "Log In" : "Logging in...");
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private String extractNestedJsonString(String json, String parentKey, String childKey) {
        String parentSearch = "\"" + parentKey + "\":{";
        int parentStart = json.indexOf(parentSearch);
        if (parentStart == -1) return null;
        return extractJsonString(json.substring(parentStart + parentSearch.length()), childKey);
    }
}