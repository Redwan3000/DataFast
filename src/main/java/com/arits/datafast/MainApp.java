package com.arits.datafast;

import com.arits.datafast.routing.SceneRouter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Register stage with router before loading any FXML
        SceneRouter.init(stage);

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("login-view.fxml")
        );
        Parent root = loader.load();

        // One Scene for the lifetime of the app — root node swapped on navigation
        Scene scene = new Scene(root, 500, 400);
        scene.getStylesheets().add(
                MainApp.class.getResource("application.css").toExternalForm()
        );

        stage.setTitle("DataFast");
        stage.setScene(scene);
        stage.show();
    }
}
