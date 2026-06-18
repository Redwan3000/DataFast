package com.arits.datafast.routing;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;


public class SceneRouter {

    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }


    public static void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneRouter.class.getResource(
                            "/com/arits/datafast/" + fxmlPath)
            );
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
            throw new RuntimeException("SceneRouter: failed to navigate to "
                    + fxmlPath + ": " + e.getMessage(), e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
