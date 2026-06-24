package com.arits.datafast.theme;

import javafx.scene.Scene;

import java.util.List;

public class ThemeManager {

    private static final String STYLE_BASE = "/com/arits/datafast/style/";
    private static final List<String> BASE_STYLESHEETS = List.of(
            STYLE_BASE + "base/typography.css",
            STYLE_BASE + "base/components.css",
            STYLE_BASE + "base/layout.css",
            STYLE_BASE + "components/navbar.css",
            STYLE_BASE + "components/module-card.css",
            STYLE_BASE + "components/otp.css"
    );
    private static boolean darkMode = false;

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggle(Scene scene) {
        darkMode = !darkMode;
        apply(scene);
    }

    public static void apply(Scene scene) {
        scene.getStylesheets().clear();


        String themePath = STYLE_BASE + (darkMode ? "theme-dark.css" : "theme-light.css");
        scene.getStylesheets().add(resolve(themePath));

        BASE_STYLESHEETS.forEach(p -> scene.getStylesheets().add(resolve(p)));
    }

    private static String resolve(String path) {
        var url = ThemeManager.class.getResource(path);
        if (url == null) throw new RuntimeException("Stylesheet not found: " + path);
        return url.toExternalForm();
    }
}