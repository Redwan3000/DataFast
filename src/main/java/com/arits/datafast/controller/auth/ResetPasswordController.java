package com.arits.datafast.controller.auth;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.auth.AuthService;
import com.arits.datafast.state.AppState;
import com.arits.datafast.util.helpers.ErrorHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class ResetPasswordController {

    private final AuthService authService = new AuthService();

    // OTP boxes
    @FXML
    private TextField otp1, otp2, otp3, otp4, otp5, otp6;

    // Password fields
    @FXML
    private PasswordField newPasswordHidden;
    @FXML
    private TextField newPasswordVisible;
    @FXML
    private PasswordField confirmPasswordHidden;
    @FXML
    private TextField confirmPasswordVisible;
    @FXML
    private FontIcon newPasswordToggle;
    @FXML
    private FontIcon confirmPasswordToggle;

    // Requirements icons
    @FXML
    private FontIcon req1Icon, req2Icon, req3Icon, req4Icon, req5Icon;

    // Error banner
    @FXML
    private HBox errorBanner;
    @FXML
    private Label errorLabel;

    // Resend
    @FXML
    private Hyperlink resendLink;
    @FXML
    private Button resetButton;
    private ErrorHelper errorHelper;
    private boolean newPasswordVisible_flag = false;
    private boolean confirmPasswordVisible_flag = false;
    private Timeline resendTimer;
    private int resendSeconds = 180;

    @FXML
    public void initialize() {

        errorHelper = new ErrorHelper(errorBanner, errorLabel);
        newPasswordVisible.textProperty().bindBidirectional(newPasswordHidden.textProperty());
        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordHidden.textProperty());

        // OTP auto-advance
        setupOtpBox(otp1, null, otp2);
        setupOtpBox(otp2, otp1, otp3);
        setupOtpBox(otp3, otp2, otp4);
        setupOtpBox(otp4, otp3, otp5);
        setupOtpBox(otp5, otp4, otp6);
        setupOtpBox(otp6, otp5, null);

        // Live password validation
        newPasswordHidden.textProperty().addListener((obs, old, val) -> updateRequirements());
        confirmPasswordHidden.textProperty().addListener((obs, old, val) -> updateRequirements());

//        countdown for otp
        startResendCountdown();
    }





    private void setupOtpBox(TextField current, TextField prev, TextField next) {
        current.textProperty().addListener((obs, oldVal, newVal) -> {
            // Only allow single digit
            if (newVal.length() > 1) {
                current.setText(newVal.substring(0, 1));
                return;
            }
            // Auto advance to next
            if (!newVal.isEmpty() && next != null) {
                next.requestFocus();
            }
        });

        current.setOnKeyPressed(e -> {
            // Backspace goes to previous
            if (e.getCode().toString().equals("BACK_SPACE")
                    && current.getText().isEmpty() && prev != null) {
                prev.requestFocus();
            }
        });
    }

    private void updateRequirements() {
        String password = newPasswordHidden.getText();
        String confirm = confirmPasswordHidden.getText();

        setReq(req1Icon, password.length() >= 8);
        setReq(req2Icon, password.matches(".*[A-Z].*"));
        setReq(req3Icon, password.matches(".*[0-9].*"));
        setReq(req4Icon, password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"));
        setReq(req5Icon, !password.isEmpty() && password.equals(confirm));

        // Enable reset button only when all pass
        resetButton.setDisable(!allRequirementsMet(password, confirm));
    }

    private void setReq(FontIcon icon, boolean met) {
        icon.setIconLiteral(met ? "gmi-check" : "gmi-close");
        icon.getStyleClass().removeAll("req-icon-pass", "req-icon-fail");
        icon.getStyleClass().add(met ? "req-icon-pass" : "req-icon-fail");
    }

    private boolean allRequirementsMet(String password, String confirm) {
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")
                && password.equals(confirm);
    }

    private void startResendCountdown() {
        resendLink.setDisable(true);
        resendSeconds = 180;
        resendTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            resendSeconds--;
            resendLink.setText("Resend OTP (" + resendSeconds + "s)");
            if (resendSeconds <= 0) {
                resendTimer.stop();
                SceneRouter.navigateBack();
                resendLink.setText("Resend OTP");
                resendLink.setDisable(false);
            }
        }));
        resendTimer.setCycleCount(Timeline.INDEFINITE);
        resendTimer.play();
    }

    @FXML
    private void handleResendOtp() {
        String email = AppState.getInstance().getTempEmail();
        new Thread(() -> {
            try {
                authService.requestOtp(email);
                Platform.runLater(this::startResendCountdown);
            } catch (Exception e) {
                errorHelper.showError(e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleNewPasswordToggle() {
        newPasswordVisible_flag = !newPasswordVisible_flag;
        newPasswordHidden.setVisible(!newPasswordVisible_flag);
        newPasswordHidden.setManaged(!newPasswordVisible_flag);
        newPasswordVisible.setVisible(newPasswordVisible_flag);
        newPasswordVisible.setManaged(newPasswordVisible_flag);
        newPasswordToggle.setIconLiteral(newPasswordVisible_flag ? "gmi-visibility" : "gmi-visibility-off");
    }

    @FXML
    private void handleConfirmPasswordToggle() {
        confirmPasswordVisible_flag = !confirmPasswordVisible_flag;
        confirmPasswordHidden.setVisible(!confirmPasswordVisible_flag);
        confirmPasswordHidden.setManaged(!confirmPasswordVisible_flag);
        confirmPasswordVisible.setVisible(confirmPasswordVisible_flag);
        confirmPasswordVisible.setManaged(confirmPasswordVisible_flag);
        confirmPasswordToggle.setIconLiteral(confirmPasswordVisible_flag ? "gmi-visibility" : "gmi-visibility-off");
    }

    @FXML
    private void handleReset() {
        String otp = otp1.getText() + otp2.getText() + otp3.getText()
                + otp4.getText() + otp5.getText() + otp6.getText();
        String password = newPasswordHidden.getText();
        String confirm = confirmPasswordHidden.getText();
        String email = AppState.getInstance().getTempEmail();

        if (otp.length() < 6) {
            errorHelper.showError("Please enter the complete 6-digit OTP.");
            return;
        }
        if (email == null) {
            errorHelper.showError("Session expired. Please start again.");
            return;
        }

        errorHelper.hideError();
        resetButton.setDisable(true);
        resetButton.setText("Resetting...");

        new Thread(() -> {
            try {
                authService.resetPassword(email, otp, password);
                AppState.getInstance().setTempEmail(null);
                Platform.runLater(() ->
                        SceneRouter.navigateTo("/auth/login-view.fxml")
                );
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resetButton.setDisable(false);
                    resetButton.setText("Reset Password");
                    errorHelper.showError(e.getMessage());
                });
            }
        }).start();
    }


}