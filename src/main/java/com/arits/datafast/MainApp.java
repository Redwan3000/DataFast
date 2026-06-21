package com.arits.datafast;

import com.arits.datafast.config.AppConfig;
import com.arits.datafast.routing.SceneRouter;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        // This is the standard Java entry point that launches the JavaFX Application thread
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. Wake up the configuration vault first so environment variables are ready
        AppConfig.getInstance();

        // 2. Give the SceneRouter control of the main window
        SceneRouter.init(primaryStage);

        // 3. Configure your OS window settings
        primaryStage.setTitle("DataFast Automation");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);

        // 4. Fire up the first view
        SceneRouter.navigateTo("/auth/login-view.fxml");

        // 5. Show the window on the screen
        primaryStage.show();
    }
}