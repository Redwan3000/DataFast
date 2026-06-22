package com.arits.datafast.controller.components;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.state.AppState;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox; // or VBox depending on your navbar layout

public class NavbarController {

    @FXML private HBox root; // Ensure this matches the fx:id in your navbar-view.fxml
    @FXML private Button backBtn;
    @FXML private Button forwardBtn;

    @FXML
    public void initialize() {
        backBtn.setDisable(!SceneRouter.canGoBack());
        forwardBtn.setDisable(!SceneRouter.canGoForward());

        bindResponsivePadding();
    }

    private void bindResponsivePadding() {
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                root.paddingProperty().bind(Bindings.createObjectBinding(
                        () -> {
                            double side = newScene.getWidth() * 0.20;
                            return new Insets(12, side, 12, side); // Adjusted 12px for top/bottom navbar layout
                        },
                        newScene.widthProperty()
                ));
            }
        });
    }

    @FXML
    private void handleHome() {
        if (AppState.getInstance().isLoggedIn()) {
            SceneRouter.navigateTo("/dashboard/dashboard-view.fxml");
        } else {
            SceneRouter.navigateTo("/auth/login-view.fxml");
        }
    }

    @FXML
    private void handleBack() {
        SceneRouter.navigateBack();
    }

    @FXML
    private void handleForward() {
        SceneRouter.navigateForward();
    }

    @FXML
    private void handleRefresh() {
        SceneRouter.refresh();
    }

    @FXML
    private void handleLogout() {
        AppState.getInstance().clear();
        SceneRouter.navigateTo("/auth/login-view.fxml");
    }
}