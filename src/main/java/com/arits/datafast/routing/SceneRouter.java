package com.arits.datafast.routing;

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

//to navigate to any page/fxml
    public static void navigateTo(String fxmlPath) {
        if (currentPath != null) {
            backStack.push(currentPath);
        }
        forwardStack.clear();
        currentPath = fxmlPath;
        load(fxmlPath);
    }

    //navbar back arrow button
    public static void navigateBack() {
        if (backStack.isEmpty()) return;
        forwardStack.push(currentPath);
        currentPath = backStack.pop();
        load(currentPath);
    }

//    navbar front arrow button
    public static void navigateForward() {
        if (forwardStack.isEmpty()) return;
        backStack.push(currentPath);
        currentPath = forwardStack.pop();
        load(currentPath);
    }


//    navbar refresh button
    public static void refresh() {
        if (currentPath != null) {
            load(currentPath);
        }
    }

    public static boolean canGoBack()    { return !backStack.isEmpty(); }
    public static boolean canGoForward() { return !forwardStack.isEmpty(); }

    private static void load(String fxmlPath) {
        try {
            String fullPath = fxmlPath.startsWith("/com/arits/datafast/views")
                    ? fxmlPath
                    : "/com/arits/datafast/views" + fxmlPath;

            URL resourceUrl = SceneRouter.class.getResource(fullPath);
            if (resourceUrl == null) {
                throw new RuntimeException("Could not find FXML file: " + fullPath);
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            if (primaryStage.getScene() == null) {
                primaryStage.setScene(new Scene(root));
            } else {
                primaryStage.getScene().setRoot(root);
            }

            com.arits.datafast.theme.ThemeManager.applyToRoot(root);
        } catch (Exception e) {
            System.err.println("Router Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}