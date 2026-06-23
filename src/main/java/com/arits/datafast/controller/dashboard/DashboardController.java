package com.arits.datafast.controller.dashboard;

import com.arits.datafast.dto.automation.AutomationModuleDto;
import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.automation.AutomationService;
import com.arits.datafast.state.AppState;
import com.arits.datafast.util.CryptoUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private static final String MODULE_CARD_FXML =
            "/com/arits/datafast/components/module-card-component.fxml";

    private final AutomationService automationService = new AutomationService();

    @FXML private HBox     loadingBox;
    @FXML private HBox     errorBanner;
    @FXML private Label    errorLabel;
    @FXML private FlowPane moduleGrid;

    @FXML
    public void initialize() {
        loadModules();
    }

    private Node buildModuleCard(AutomationModuleDto.Module module) {
        try {
            java.net.URL fxmlUrl = getClass().getResource(MODULE_CARD_FXML);
            if (fxmlUrl == null) {
                throw new IllegalStateException(
                        "Module card FXML not found on classpath: " + MODULE_CARD_FXML
                                + " (check the file exists under src/main/resources" + MODULE_CARD_FXML
                                + " and that the project has been rebuilt)"
                );
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node root = loader.load();

            ModuleCardController controller = loader.getController();
            controller.bind(module, this::handleSubModuleLaunch);

            return root;

        } catch (Exception e) {
            log.error("[Dashboard] Failed to build card for module '{}': {}",
                    module.name(), e.getMessage(), e);
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Private — rendering
    // -------------------------------------------------------------------------

    private void renderModules(List<AutomationModuleDto.Module> modules) {
        moduleGrid.getChildren().clear();

        if (modules.isEmpty()) {
            showLoading(false);
            showError(true, "No portals are configured for your account.");
            return;
        }

        for (AutomationModuleDto.Module module : modules) {
            Node card = buildModuleCard(module);
            if (card != null) {
                moduleGrid.getChildren().add(card);
            }
        }
        showLoading(false);
        showGrid(true);
    }

    // -------------------------------------------------------------------------
    // loading modules
    // -------------------------------------------------------------------------

    private void loadModules() {
        showLoading(true);

        new Thread(() -> {
            try {
                List<AutomationModuleDto.Module> modules = automationService.fetchModules();
                Platform.runLater(() -> renderModules(modules));
            } catch (Exception e) {
                log.error("[Dashboard] Failed to load modules: {}", e.getMessage());
                Platform.runLater(() -> {
                    showLoading(false);
                    showError(true, e.getMessage());
                });
            }
        }, "dashboard-module-loader").start();
    }



    // -------------------------------------------------------------------------
    // Private — sub-module launch handler
    // -------------------------------------------------------------------------


    private void handleSubModuleLaunch(AutomationModuleDto.SubModule subModule) {
        log.info("[Dashboard] User selected automation: id={}, name='{}'",
                subModule.id(), subModule.name());

        // TODO: Pass subModule details to the automation runner / browser session.
        //       For now, navigate to a placeholder automation view.
        // SceneRouter.navigateTo("/automation/automation-runner-view.fxml");
    }

    // -------------------------------------------------------------------------
    // FXML event handlers
    // -------------------------------------------------------------------------

    @FXML
    private void handleLogout() {
        AppState.getInstance().clear();
        log.info("[Dashboard] User logged out.");
        SceneRouter.navigateTo("/auth/login-view.fxml");
    }

    // -------------------------------------------------------------------------
    // Private — UI state helpers
    // -------------------------------------------------------------------------

    private void showLoading(boolean visible) {
        loadingBox.setVisible(visible);
        loadingBox.setManaged(visible);
    }

    private void showGrid(boolean visible) {
        moduleGrid.setVisible(visible);
        moduleGrid.setManaged(visible);
    }

    private void showError(boolean visible, String message) {
        errorBanner.setVisible(visible);
        errorBanner.setManaged(visible);
        if (visible && message != null) {
            errorLabel.setText(message);
        }
    }
}