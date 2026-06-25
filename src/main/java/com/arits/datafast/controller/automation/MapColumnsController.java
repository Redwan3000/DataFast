package com.arits.datafast.controller.automation;

import com.arits.datafast.dto.automation.FormFieldMappingDto;
import com.arits.datafast.routing.SceneRouter;
import com.arits.datafast.service.automation.AutomationService;
import com.arits.datafast.service.automation.ColumnMatcher;
import com.arits.datafast.state.AppState;
import com.arits.datafast.state.AutomationState;
import com.arits.datafast.state.FieldMappingRow;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class MapColumnsController {

    private static final Logger log = LoggerFactory.getLogger(MapColumnsController.class);
    private static final int PREVIEW_ROW_LIMIT = 10;

    private final AutomationService automationService = new AutomationService();
    private final ColumnMatcher columnMatcher = new ColumnMatcher();

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
    private Label progressLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextField filterField;
    @FXML
    private VBox fieldRowsContainer;
    @FXML
    private TitledPane previewTitledPane;
    @FXML
    private Button saveMappingButton;
    @FXML
    private Button continueButton;

    private List<String> excelHeaders;
    private List<List<String>> excelRows;
    private List<FieldMappingRow> mappingRows;
    private TableView<List<String>> previewTable;

    @FXML
    public void initialize() {
        AutomationState ctx = AutomationState.getAutomationState();

        if (ctx.getSelectedFile() == null || ctx.getExcelHeaders() == null) {
            log.warn("[MapColumns] No uploaded file in state — redirecting to Upload.");
            SceneRouter.navigateTo("/automation/upload-view.fxml");
            return;
        }
        breadcrumbModule.setText(ctx.getModuleName());
        breadcrumbAutomationGroup.setText("EXP Automation");
        breadcrumbCurrent.setText(ctx.getSubModuleName());

        automationNameLabel.setText(ctx.getSubModuleName());
        automationDescLabel.setText(ctx.getSubModuleDescription());

        excelHeaders = ctx.getExcelHeaders();
        excelRows = ctx.getExcelRows();

        File file = ctx.getSelectedFile();
        fileInfoLabel.setText(file.getName() + "  ·  " + ctx.getSheetName() + "  ·  " + excelRows.size() + " rows");

        continueButton.setDisable(true);
        filterField.textProperty().addListener((obs, oldV, newV) -> applyFilter());

        buildPreviewTable();
        loadFieldMappings();
    }

    // -------------------------------------------------------------------------
    // Breadcrumb
    // -------------------------------------------------------------------------

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
        SceneRouter.navigateBack(); // back to Upload
    }

    // -------------------------------------------------------------------------
    // Loading required fields + auto-matching
    // -------------------------------------------------------------------------

    private void loadFieldMappings() {
        AutomationState ctx = AutomationState.getAutomationState();
        int automationId = ctx.getAutomationId();
        int companyId = AppState.getAppState().getCompanyId(); // TODO: confirm this getter name on AppState

        new Thread(() -> {
            try {
                List<FormFieldMappingDto.Mapping> apiRows =
                        automationService.fetchFieldMappings(automationId, companyId);

                List<FieldMappingRow> rows = apiRows.stream()
                        .map(m -> new FieldMappingRow(
                                m.genericField().id(),
                                m.genericField().name(),
                                m.excelColumn(),
                                m.actionType() != null ? m.actionType().name() : null,
                                m.selector()))
                        .collect(Collectors.toList());

                columnMatcher.autoMatch(rows, excelHeaders);

                Platform.runLater(() -> {
                    mappingRows = rows;
                    ctx.setFieldMappingRows(rows); // same instances — combo edits stay in sync automatically
                    renderFieldRows();
                    refreshPreviewHighlight();
                });

            } catch (Exception e) {
                log.error("[MapColumns] Failed to load field mappings: {}", e.getMessage(), e);
                Platform.runLater(() ->
                        progressLabel.setText("Could not load required fields — " + e.getMessage()));
            }
        }, "field-mapping-loader").start();
    }

    @FXML
    private void handleAutoMatch() {
        if (mappingRows == null) return;
        columnMatcher.autoMatch(mappingRows, excelHeaders);
        renderFieldRows();
        refreshPreviewHighlight();
        applyFilter();
    }

    // -------------------------------------------------------------------------
    // Field row rendering
    // -------------------------------------------------------------------------

    private void renderFieldRows() {
        fieldRowsContainer.getChildren().clear();
        for (FieldMappingRow row : mappingRows) {
            fieldRowsContainer.getChildren().add(buildFieldRow(row));
        }
        updateProgress();
    }

    private Node buildFieldRow(FieldMappingRow row) {
        HBox container = new HBox(12);
        container.setUserData(row);
        container.getStyleClass().add("mapping-row");
        container.setAlignment(Pos.CENTER_LEFT);

        FontIcon statusIcon = new FontIcon();
        statusIcon.setIconSize(16);
        updateStatusIcon(statusIcon, row.isMapped());

        Label nameLabel = new Label(row.getFieldName());
        nameLabel.getStyleClass().add("mapping-field-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        FontIcon arrowIcon = new FontIcon("gmi-arrow-forward");
        arrowIcon.setIconSize(14);
        arrowIcon.getStyleClass().add("mapping-arrow-icon");

        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(excelHeaders));
        combo.setPromptText("Pick a column from your file...");
        combo.getStyleClass().add("mapping-combo");
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setValue(row.getSelectedExcelColumn());

        Button clearBtn = new Button(null, new FontIcon("gmi-close"));
        clearBtn.getStyleClass().add("mapping-clear-btn");
        clearBtn.visibleProperty().bind(combo.valueProperty().isNotNull());
        clearBtn.managedProperty().bind(clearBtn.visibleProperty());
        clearBtn.setOnAction(e -> combo.setValue(null));

        combo.valueProperty().addListener((obs, oldV, newV) -> {
            row.setSelectedExcelColumn(newV);
            updateStatusIcon(statusIcon, row.isMapped());
            updateProgress();
            refreshPreviewHighlight();
        });

        HBox comboArea = new HBox(4, combo, clearBtn);
        comboArea.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(comboArea, Priority.ALWAYS);

        container.getChildren().addAll(statusIcon, nameLabel, arrowIcon, comboArea);
        return container;
    }

    private void updateStatusIcon(FontIcon icon, boolean mapped) {
        icon.getStyleClass().removeAll("status-icon-mapped", "status-icon-unmapped");
        if (mapped) {
            icon.setIconLiteral("gmi-check-circle");
            icon.getStyleClass().add("status-icon-mapped");
        } else {
            icon.setIconLiteral("gmi-radio-button-unchecked");
            icon.getStyleClass().add("status-icon-unmapped");
        }
    }

    private void updateProgress() {
        long mapped = mappingRows.stream().filter(FieldMappingRow::isMapped).count();
        int total = mappingRows.size();
        progressLabel.setText(mapped + " / " + total + " mapped");
        progressBar.setProgress(total == 0 ? 0 : (double) mapped / total);
        continueButton.setDisable(mapped < total);
    }

    private void applyFilter() {
        if (fieldRowsContainer == null) return;
        String query = filterField.getText() == null ? "" : filterField.getText().trim().toLowerCase();
        for (Node node : fieldRowsContainer.getChildren()) {
            FieldMappingRow row = (FieldMappingRow) node.getUserData();
            boolean matches = query.isEmpty() || row.getFieldName().toLowerCase().contains(query);
            node.setVisible(matches);
            node.setManaged(matches);
        }
    }

    // -------------------------------------------------------------------------
    // Preview table
    // -------------------------------------------------------------------------

    private void buildPreviewTable() {
        TableView<List<String>> table = new TableView<>();
        table.setEditable(false);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("preview-table");

        for (int i = 0; i < excelHeaders.size(); i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> col = new TableColumn<>(excelHeaders.get(i));
            col.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                    colIndex < data.getValue().size() ? data.getValue().get(colIndex) : ""));
            col.setPrefWidth(130);
            table.getColumns().add(col);
        }

        int previewCount = Math.min(PREVIEW_ROW_LIMIT, excelRows.size());
        table.setItems(FXCollections.observableArrayList(excelRows.subList(0, previewCount)));

        previewTitledPane.setText("Preview of your file (first " + previewCount + " of " + excelRows.size() + " rows)");
        previewTitledPane.setContent(table);
        previewTitledPane.setExpanded(false);

        this.previewTable = table;
    }

    private void refreshPreviewHighlight() {
        if (previewTable == null || mappingRows == null) return;
        Set<String> mappedHeaders = mappingRows.stream()
                .filter(FieldMappingRow::isMapped)
                .map(FieldMappingRow::getSelectedExcelColumn)
                .collect(Collectors.toSet());

        for (TableColumn<List<String>, ?> col : previewTable.getColumns()) {
            col.getStyleClass().remove("preview-col-mapped");
            if (mappedHeaders.contains(col.getText())) {
                col.getStyleClass().add("preview-col-mapped");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Footer actions
    // -------------------------------------------------------------------------

    @FXML
    private void handleSaveMapping() {
        // Mapping already lives in AutomationState in real time as the user
        // picks columns above. Wire FormFieldMappings.upsert(...) here later
        // if you want it to persist as the company's default for next time.
        log.info("[MapColumns] {} / {} fields mapped",
                mappingRows.stream().filter(FieldMappingRow::isMapped).count(), mappingRows.size());

        saveMappingButton.setText("Saved");
        saveMappingButton.setDisable(true);
        new Thread(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> {
                saveMappingButton.setText("Save mapping");
                saveMappingButton.setDisable(false);
            });
        }).start();
    }

    @FXML
    private void handleContinue() {
        if (!AutomationState.getAutomationState().allFieldsMapped()) return; // guard — button is disabled anyway
        SceneRouter.navigateTo("/automation/review-run-view.fxml");
    }
}