package com.arits.datafast.controller.automation;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.excel.ExcelService;
import com.arits.datafast.state.AutomationState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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


public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private static final List<String> ACCEPTED_EXTENSIONS =
            List.of(".xlsx", ".xlsm", ".xls");

    private final ExcelService excelService = new ExcelService();

    @FXML
    private Label breadcrumbModule;
    @FXML
    private Label breadcrumbAutomationGroup;
    @FXML
    private Label breadcrumbCurrent;

    @FXML
    private Label automationNameLabel;
    @FXML
    private Label automationDescLabel;

    @FXML
    private StackPane dropZone;
    @FXML
    private Button chooseFileButton;

    private boolean parsing = false;


    @FXML
    public void initialize() {
        AutomationState ctx = AutomationState.getAutomationState();

        if (ctx.getAutomationId() == 0) {
            log.warn("[Upload] No automation selected — redirecting to dashboard.");
            SceneRouter.navigateTo("/dashboard/dashboard-view.fxml");
            return;
        }


        breadcrumbModule.setText(ctx.getModuleName());
        breadcrumbAutomationGroup.setText("EXP Automation"); // group label — refine later
        breadcrumbCurrent.setText(ctx.getSubModuleName());

        automationNameLabel.setText(ctx.getSubModuleName());
        automationDescLabel.setText(ctx.getSubModuleDescription());


        ctx.clearUploadedFileData();
    }

    @FXML
    private void handleBreadcrumbDashboard() {
        SceneRouter.navigateTo("/dashboard/dashboard-view.fxml");
    }

    @FXML
    private void handleBreadcrumbModule() {
        SceneRouter.navigateBack();
    }

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

        File file = chooser.showOpenDialog(SceneRouter.getPrimaryStage());
        if (file != null) {
            processFile(file);
        }
    }

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
            File file = db.getFiles().get(0);
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
        if (parsing) return; // ignore double-clicks/drops mid-parse
        if (!isValidExtension(file)) {
            showDropZoneError();
            return;
        }

        parsing = true;
        chooseFileButton.setDisable(true);
        log.info("[Upload] File accepted: {}", file.getAbsolutePath());

        new Thread(() -> {
            try {
                ExcelService.ParsedExcel parsed = excelService.parse(file);

                Platform.runLater(() -> {
                    AutomationState ctx = AutomationState.getAutomationState();
                    ctx.setSelectedFile(file);
                    ctx.setSheetName(parsed.sheetName());
                    ctx.setExcelHeaders(parsed.headers());
                    ctx.setExcelRows(parsed.rows());

                    SceneRouter.navigateTo("/automation/map-columns-view.fxml");
                });

            } catch (Exception e) {
                log.error("[Upload] Failed to read Excel file '{}': {}", file.getName(), e.getMessage(), e);
                Platform.runLater(() -> {
                    parsing = false;
                    chooseFileButton.setDisable(false);
                    showDropZoneError();
                });
            }
        }, "excel-parser").start();
    }

    private boolean isValidExtension(File file) {
        String name = file.getName().toLowerCase();
        return ACCEPTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private void showDropZoneError() {
        dropZone.getStyleClass().add("drop-zone-error");
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> dropZone.getStyleClass().remove("drop-zone-error"));
        }).start();
    }
}