package com.arits.datafast.controller.automation;

import com.arits.datafast.dto.automation.AutomationModuleDto;
import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.state.AutomationContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Controller for automation-runner-view.fxml — Step 1: Upload.
 *
 * Receives context (module + subModule) from DashboardController via
 * AutomationContext before the scene loads.
 *
 * Responsibilities:
 *   - Populate breadcrumb and page title from context
 *   - Accept an Excel file via file picker or drag-and-drop
 *   - Validate extension (.xlsx / .xlsm / .xls)
 *   - Store the chosen file in AutomationContext and navigate to Step 2
 */
public class AutomationRunnerController {

    private static final Logger log = LoggerFactory.getLogger(AutomationRunnerController.class);

    private static final List<String> ACCEPTED_EXTENSIONS =
            List.of(".xlsx", ".xlsm", ".xls");

    // ── Breadcrumb ──────────────────────────────────────────────────────────
    @FXML private Label breadcrumbModule;
    @FXML private Label breadcrumbAutomationGroup;
    @FXML private Label breadcrumbCurrent;

    // ── Page title ──────────────────────────────────────────────────────────
    @FXML private Label automationNameLabel;
    @FXML private Label automationDescLabel;

    // ── Drop zone ───────────────────────────────────────────────────────────
    @FXML private StackPane dropZone;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        AutomationContext ctx = AutomationContext.getInstance();

        // Breadcrumb
        breadcrumbModule.setText(ctx.getModuleName());
        breadcrumbAutomationGroup.setText("EXP Automation");   // group label — refine later
        breadcrumbCurrent.setText(ctx.getSubModuleName());

        // Title
        automationNameLabel.setText(ctx.getSubModuleName());
        automationDescLabel.setText(ctx.getSubModuleDescription());
    }

    // -------------------------------------------------------------------------
    // Breadcrumb navigation
    // -------------------------------------------------------------------------

    @FXML
    private void handleBreadcrumbDashboard() {
        SceneRouter.navigateTo("/dashboard/dashboard-view.fxml");
    }

    @FXML
    private void handleBreadcrumbModule() {
        SceneRouter.navigateBack();
    }

    // -------------------------------------------------------------------------
    // File chooser (button click)
    // -------------------------------------------------------------------------

    @FXML
    private void handleChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Excel File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Excel Files (*.xlsx, *.xlsm, *.xls)",
                        "*.xlsx", "*.xlsm", "*.xls"
                )
        );

        File file = chooser.showOpenDialog(
                SceneRouter.getPrimaryStage()
        );

        if (file != null) {
            processFile(file);
        }
    }

    // -------------------------------------------------------------------------
    // Drag-and-drop handlers
    // -------------------------------------------------------------------------

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleDragEntered(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            dropZone.getStyleClass().add("drop-zone-hover");
        }
        event.consume();
    }

    @FXML
    private void handleDragExited(DragEvent event) {
        dropZone.getStyleClass().remove("drop-zone-hover");
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        dropZone.getStyleClass().remove("drop-zone-hover");
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            File file = db.getFiles().get(0);   // take first file only
            if (isValidExtension(file)) {
                processFile(file);
                success = true;
            } else {
                log.warn("[Upload] Rejected file: {}. Not an accepted Excel format.", file.getName());
                showDropZoneError();
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void processFile(File file) {
        log.info("[Upload] File accepted: {}", file.getAbsolutePath());

        // Store in context so Step 2 (Map Columns) can read it
        AutomationContext.getInstance().setSelectedFile(file);

        // Advance to Step 2
        SceneRouter.navigateTo("/automation/map-columns-view.fxml");
    }

    private boolean isValidExtension(File file) {
        String name = file.getName().toLowerCase();
        return ACCEPTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private void showDropZoneError() {
        dropZone.getStyleClass().add("drop-zone-error");
        // Remove error style after 2 seconds
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() ->
                    dropZone.getStyleClass().remove("drop-zone-error")
            );
        }).start();
    }
}