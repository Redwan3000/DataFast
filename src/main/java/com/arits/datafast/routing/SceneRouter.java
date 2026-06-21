package com.arits.datafast.routing;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class SceneRouter {

    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(String fxmlPath) {
        try {
            // Ensure path starts with /com/arits/datafast/views
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
        } catch (Exception e) {
            System.err.println("Router Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}