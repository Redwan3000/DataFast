package com.arits.datafast.routing;

import com.arits.datafast.theme.ThemeManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

public class SceneRouter {

    private static final Deque<String> backStack = new ArrayDeque<>();
    private static final Deque<String> forwardStack = new ArrayDeque<>();

    private static String currentPath;
    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(String fxmlPath) {
        if (currentPath != null) backStack.push(currentPath);
        forwardStack.clear();
        currentPath = fxmlPath;
        load(fxmlPath);
    }

    public static void navigateBack() {
        if (backStack.isEmpty()) return;
        forwardStack.push(currentPath);
        currentPath = backStack.pop();
        load(currentPath);
    }

    public static void navigateForward() {
        if (forwardStack.isEmpty()) return;
        backStack.push(currentPath);
        currentPath = forwardStack.pop();
        load(currentPath);
    }

    public static void refresh() {
        if (currentPath != null) load(currentPath);
    }

    public static boolean canGoBack() {
        return !backStack.isEmpty();
    }

    public static boolean canGoForward() {
        return !forwardStack.isEmpty();
    }

    private static void load(String fxmlPath) {
        try {
            String fullPath = fxmlPath.startsWith("/com/arits/datafast/views")
                    ? fxmlPath
                    : "/com/arits/datafast/views" + fxmlPath;

            URL resourceUrl = SceneRouter.class.getResource(fullPath);
            if (resourceUrl == null) {
                throw new RuntimeException("FXML not found: " + fullPath);
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                ThemeManager.apply(scene);
            } else {
                primaryStage.getScene().setRoot(root);
                ThemeManager.apply(primaryStage.getScene());
            }

        } catch (Exception e) {
            System.err.println("SceneRouter error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}