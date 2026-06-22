package com.arits.datafast.controller.components;

import com.arits.datafast.theme.ThemeManager;
import javafx.fxml.FXML;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

public class HeaderController {

    @FXML private HBox root;
    @FXML private FontIcon themeIcon;

    @FXML
    public void initialize() {
        updateIcon();
        bindResponsivePadding();
    }

    private void bindResponsivePadding() {
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                root.paddingProperty().bind(Bindings.createObjectBinding(
                        () -> {
                            double side = newScene.getWidth() * 0.20;
                            return new Insets(16, side, 16, side);
                        },
                        newScene.widthProperty()
                ));
            }
        });
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.toggle();
        updateIcon();
    }

    private void updateIcon() {
        themeIcon.setIconLiteral(ThemeManager.isDarkMode() ? "gmi-brightness-3" : "gmi-wb-sunny");
    }
}