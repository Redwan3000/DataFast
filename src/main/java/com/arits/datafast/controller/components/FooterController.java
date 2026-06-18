package com.arits.datafast.controller.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class FooterController implements Initializable {

    @FXML Label footerLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        int currentYear = LocalDateTime.now().getYear();

        footerLabel.setText("© "+ currentYear+" DataFast LImited. All rights reserved.");

    }
}
