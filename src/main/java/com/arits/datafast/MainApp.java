package com.arits.datafast;

import com.arits.datafast.config.AppConfig;
import com.arits.datafast.routing.SceneRouter;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

//        firing properties configuration
        AppConfig.getAppConfig();

//        passing the main window to scene Router
        SceneRouter.init(stage);

//        configuring the window
        stage.setTitle("DataFast Automation");
        stage.setMinWidth(900);
        stage.setMinHeight(700);

//        Firing the landing page (1st page)
        SceneRouter.navigateTo("/auth/login-view.fxml");

//        displaying the window
        stage.show();
    }
}