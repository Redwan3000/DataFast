package com.arits.datafast.controller.dashboard;

import com.arits.datafast.dto.automation.AutomationModuleDto;
import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.automation.AutomationService;
import com.arits.datafast.state.AppState;
import com.arits.datafast.state.AutomationState;
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

    // Cached after first API fetch — used by resolveModuleName()
    private List<AutomationModuleDto.Module> cachedModules = List.of();

    @FXML
    private HBox loadingBox;
    @FXML
    private HBox errorBanner;
    @FXML
    private Label errorLabel;
    @FXML
    private FlowPane moduleGrid;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        logWelcome();
        loadModules();
    }

    // -------------------------------------------------------------------------
    // Private — welcome log
    // -------------------------------------------------------------------------

    private void logWelcome() {
        String encryptedName = AppState.getAppState().getUserName();
        if (encryptedName != null && !encryptedName.isBlank()) {
            try {
                String realName = CryptoUtil.decryptAES(encryptedName);
                log.info("[Dashboard] Session active for: {}", realName);
            } catch (Exception e) {
                log.warn("[Dashboard] Could not decrypt user name: {}", e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private — data loading
    // -------------------------------------------------------------------------

    private void loadModules() {
        showLoading(true);
        showError(false, null);

        new Thread(() -> {
            try {
                List<AutomationModuleDto.Module> modules = automationService.fetchModules();
                Platform.runLater(() -> renderModules(modules));
            } catch (Exception e) {
                log.error("[Dashboard] Failed to load modules: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    showLoading(false);
                    showError(true, e.getMessage());
                });
            }
        }, "dashboard-module-loader").start();
    }

    // -------------------------------------------------------------------------
    // Private — rendering
    // -------------------------------------------------------------------------

    private void renderModules(List<AutomationModuleDto.Module> modules) {
        cachedModules = modules;  // cache FIRST before building cards
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

    private Node buildModuleCard(AutomationModuleDto.Module module) {
        try {
            java.net.URL fxmlUrl = getClass().getResource(MODULE_CARD_FXML);
            if (fxmlUrl == null) {
                throw new IllegalStateException(
                        "Module card FXML not found on classpath: " + MODULE_CARD_FXML
                                + " — check the file exists under src/main/resources"
                                + MODULE_CARD_FXML + " and that the project has been rebuilt."
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
    // Private — sub-module launch handler
    // -------------------------------------------------------------------------

    private void handleSubModuleLaunch(AutomationModuleDto.SubModule subModule) {
        log.info("[Dashboard] Launching automation id={} name='{}'",
                subModule.id(), subModule.name());

        AutomationState.getAutomationState().start(
                subModule.moduleId(),
                resolveModuleName(subModule.moduleId()),
                subModule.id(),
                subModule.name(),
                subModule.description()
        );

        SceneRouter.navigateTo("/automation/upload-view.fxml");
    }

    private String resolveModuleName(int moduleId) {
        return cachedModules.stream()
                .filter(m -> m.id() == moduleId)
                .map(AutomationModuleDto.Module::name)
                .findFirst()
                .orElse("Portal");
    }

    // -------------------------------------------------------------------------
    // FXML event handlers
    // -------------------------------------------------------------------------

    @FXML
    private void handleLogout() {
        AppState.getAppState().clearSession();
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