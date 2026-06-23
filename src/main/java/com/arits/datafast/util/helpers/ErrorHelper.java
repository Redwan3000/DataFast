package com.arits.datafast.util.helpers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;



public class ErrorHelper {


    private HBox errorBanner;
    private Label errorLabel;

    // 1. Keep the empty constructor just in case other parts of your code use it
    public ErrorHelper() {
    }

    // 2. Add the constructor your controllers are looking for
    public ErrorHelper(HBox errorBox, Label errorLabel) {
        this.errorBanner = errorBox;
        this.errorLabel = errorLabel;
    }

    public void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorBanner.setVisible(true);
            errorBanner.setManaged(true);
        });
    }

    public void hideError() {
        Platform.runLater(() -> {
            errorBanner.setVisible(false);
            errorBanner.setManaged(false);
        });
    }
}
