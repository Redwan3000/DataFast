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

/**
 * Controller for dashboard-view.fxml.
 *
 * On initialize:
 *   1. Displays the welcome label using the decrypted name from AppState.
 *   2. Shows a loading indicator.
 *   3. Fires an async API call to fetch automation modules.
 *   4. On success: hides loader, dynamically creates a ModuleCard per module.
 *   5. On failure: hides loader, shows the error banner.
 *
 * Each module card is a self-contained FXML component loaded from
 * module-card-component.fxml and controlled by ModuleCardController.
 * The dashboard itself stays clean — it only manages layout and lifecycle.
 */
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private static final String MODULE_CARD_FXML =
            "/com/arits/datafast/views/components/module-card-component.fxml";

    private final AutomationService automationService = new AutomationService();

    @FXML private HBox     loadingBox;
    @FXML private HBox     errorBanner;
    @FXML private Label    errorLabel;
    @FXML private FlowPane moduleGrid;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        logWelcome();
        loadModules();
    }

    // -------------------------------------------------------------------------
    // Private — setup & data loading
    // -------------------------------------------------------------------------

    private void logWelcome() {
        String encryptedName = AppState.getInstance().getUserName();
        if (encryptedName != null && !encryptedName.isBlank()) {
            try {
                String realName = CryptoUtil.decryptAES(encryptedName);
                log.info("[Dashboard] Session active for: {}", realName);
            } catch (Exception e) {
                log.warn("[Dashboard] Could not decrypt user name: {}", e.getMessage());
            }
        }
    }

    /**
     * Fires the API call on a background thread, then updates the UI on the
     * JavaFX Application Thread via Platform.runLater.
     */
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

    /**
     * Clears any existing cards, then creates one ModuleCard component per
     * module returned by the API and adds it to the FlowPane grid.
     */
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

    /**
     * Loads module-card-component.fxml, retrieves its controller, binds the
     * module data to it, and returns the root node ready for insertion.
     *
     * @return the card root node, or null if the FXML failed to load
     */
    private Node buildModuleCard(AutomationModuleDto.Module module) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(MODULE_CARD_FXML)
            );
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

    /**
     * Called when the user clicks on a sub-module row inside any card.
     * This is where you trigger the browser automation session.
     *
     * @param subModule the automation the user wants to run
     */
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