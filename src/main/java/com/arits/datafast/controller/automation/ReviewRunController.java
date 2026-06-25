package com.arits.datafast.controller.automation;

import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.automation.AutomationLogSink;
import com.arits.datafast.service.automation.AutomationRunner;
import com.arits.datafast.service.automation.StubAutomationRunner;
import com.arits.datafast.state.AutomationState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewRunController implements AutomationLogSink {

    private static final Logger log = LoggerFactory.getLogger(ReviewRunController.class);

    // swap for the real engine once it exists — nothing else here needs to change
    private final AutomationRunner automationRunner = new StubAutomationRunner();

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
    private Label fileInfoLabel;

    @FXML
    private VBox reviewView;
    @FXML
    private VBox logView;

    @FXML
    private Label reviewFileLabel;
    @FXML
    private Label reviewSheetLabel;
    @FXML
    private Label reviewRowsLabel;
    @FXML
    private Label reviewFieldsLabel;
    @FXML
    private TextField startRowField;
    @FXML
    private Button startAutomationButton;

    @FXML
    private Label logStatusLabel;
    @FXML
    private TextFlow logFlow;
    @FXML
    private ScrollPane logScrollPane;

    @FXML
    public void initialize() {
        AutomationState ctx = AutomationState.getAutomationState();

        if (!ctx.allFieldsMapped()) {
            log.warn("[ReviewRun] Mapping incomplete — redirecting to Map Columns.");
            SceneRouter.navigateTo("/automation/map-columns-view.fxml");
            return;
        }

        breadcrumbModule.setText(ctx.getModuleName());
        breadcrumbAutomationGroup.setText("EXP Automation");
        breadcrumbCurrent.setText(ctx.getSubModuleName());
        automationNameLabel.setText(ctx.getSubModuleName());
        automationDescLabel.setText(ctx.getSubModuleDescription());

        fileInfoLabel.setText(ctx.getSelectedFile().getName() + "  ·  " + ctx.getSheetName()
                + "  ·  " + ctx.getExcelRows().size() + " rows");

        reviewFileLabel.setText(ctx.getSelectedFile().getName());
        reviewSheetLabel.setText(ctx.getSheetName());
        reviewRowsLabel.setText(String.valueOf(ctx.getExcelRows().size()));
        int mappedCount = ctx.getFieldMappingRows().size();
        reviewFieldsLabel.setText(mappedCount + "/" + mappedCount);

        startRowField.setText(String.valueOf(ctx.getStartRow()));
        showReview();
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
    private void handleChangeFile() {
        SceneRouter.navigateBack();
    }

    @FXML
    private void handleBackToMapping() {
        SceneRouter.navigateBack();
    }

    @FXML
    private void handleStartAutomation() {
        int startRow = parseStartRow();
        if (startRow < 1) {
            startRowField.getStyleClass().add("input-error");
            return;
        }
        startRowField.getStyleClass().remove("input-error");

        AutomationState ctx = AutomationState.getAutomationState();
        ctx.setStartRow(startRow);

        showLog();
        logFlow.getChildren().clear();
        startAutomationButton.setDisable(true);

        automationRunner.run(ctx.getSelectedFile(), ctx.getFieldMappingRows(), startRow, this);
    }

    private int parseStartRow() {
        try {
            return Integer.parseInt(startRowField.getText().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    // -- AutomationLogSink — engine calls these from a background thread --

    @Override
    public void log(LogLevel level, String message) {
        Platform.runLater(() -> {
            Text line = new Text(message + "\n");
            line.getStyleClass().add("log-line-" + level.name().toLowerCase());
            logFlow.getChildren().add(line);
            logScrollPane.setVvalue(1.0);
        });
    }

    @Override
    public void onFinished(boolean success, String summary) {
        Platform.runLater(() -> {
            logStatusLabel.setText(success ? "Completed" : "Failed");
            logStatusLabel.getStyleClass().setAll(success ? "log-status-success" : "log-status-error");
            log(success ? LogLevel.SUCCESS : LogLevel.ERROR, summary);
        });
    }

    private void showReview() {
        reviewView.setVisible(true);
        reviewView.setManaged(true);
        logView.setVisible(false);
        logView.setManaged(false);
    }

    private void showLog() {
        reviewView.setVisible(false);
        reviewView.setManaged(false);
        logView.setVisible(true);
        logView.setManaged(true);
    }
}