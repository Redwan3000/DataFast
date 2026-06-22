package com.arits.datafast.theme;

import javafx.scene.Parent;
import java.net.URL;

public class ThemeManager {

    private static final String DARK_THEME_PATH = "/com/arits/datafast/style/dark-theme.css";

    private static boolean darkMode = false;
    private static Parent currentRoot;

    public static boolean isDarkMode() {
        return darkMode;
    }

    /** Call this every time a new root is loaded so the active theme re-applies. */
    public static void applyToRoot(Parent root) {
        currentRoot = root;
        refreshStylesheet();
    }

    public static void toggle() {
        darkMode = !darkMode;
        refreshStylesheet();
    }

    private static void refreshStylesheet() {
        if (currentRoot == null) return;
        URL darkUrl = ThemeManager.class.getResource(DARK_THEME_PATH);
        if (darkUrl == null) {
            System.err.println("ThemeManager: dark-theme.css not found at " + DARK_THEME_PATH);
            return;
        }
        String darkHref = darkUrl.toExternalForm();
        if (darkMode) {
            if (!currentRoot.getStylesheets().contains(darkHref)) {
                currentRoot.getStylesheets().add(darkHref);
            }
        } else {
            currentRoot.getStylesheets().remove(darkHref);
        }
    }
}