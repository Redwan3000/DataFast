package com.arits.datafast.controller.dashboard;

import com.arits.datafast.dto.automation.AutomationModuleDto;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for module-card-component.fxml.
 *
 * Populated externally via {@link #bind(AutomationModuleDto.Module, Consumer)}
 * after the FXML is loaded by DashboardController.
 */
public class ModuleCardController {

    @FXML private Label moduleNameLabel;
    @FXML private Label moduleIconLabel;
    @FXML private VBox  subModuleContainer;

    /** The currently highlighted sub-module row (for hover/selection state). */
    private HBox selectedRow;

    /**
     * Binds this card to a module's data and wires up sub-module click callbacks.
     *
     * @param module   the module data from the API
     * @param onLaunch callback invoked with the clicked sub-module when the user selects one
     */
    public void bind(AutomationModuleDto.Module module, Consumer<AutomationModuleDto.SubModule> onLaunch) {
        moduleNameLabel.setText(module.name());

        // Hide the plain text icon label — icon is set via ikonli or left as initials
        moduleIconLabel.setVisible(false);
        moduleIconLabel.setManaged(false);

        buildSubModuleRows(module.automations(), onLaunch);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void buildSubModuleRows(List<AutomationModuleDto.SubModule> subModules,
                                    Consumer<AutomationModuleDto.SubModule> onLaunch) {
        subModuleContainer.getChildren().clear();

        if (subModules == null || subModules.isEmpty()) {
            Label empty = new Label("No automations available");
            empty.getStyleClass().add("sub-module-empty");
            VBox.setMargin(empty, new javafx.geometry.Insets(12, 16, 12, 16));
            subModuleContainer.getChildren().add(empty);
            return;
        }

        for (AutomationModuleDto.SubModule subModule : subModules) {
            HBox row = buildRow(subModule, onLaunch);
            subModuleContainer.getChildren().add(row);
        }
    }

    private HBox buildRow(AutomationModuleDto.SubModule subModule,
                          Consumer<AutomationModuleDto.SubModule> onLaunch) {
        HBox row = new HBox();
        row.getStyleClass().add("sub-module-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(8.0);
        row.setPadding(new javafx.geometry.Insets(11, 16, 11, 16));

        Label nameLabel = new Label(subModule.name());
        nameLabel.getStyleClass().add("sub-module-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        FontIcon arrowIcon = new FontIcon("gmi-chevron-right");
        arrowIcon.getStyleClass().add("sub-module-arrow");
        arrowIcon.setIconSize(16);
        arrowIcon.setVisible(false); // only shown on hover/selection

        row.getChildren().addAll(nameLabel, arrowIcon);

        // Hover: show arrow + style change
        row.setOnMouseEntered(e -> {
            if (row != selectedRow) {
                row.getStyleClass().add("sub-module-row-hover");
            }
            arrowIcon.setVisible(true);
        });
        row.setOnMouseExited(e -> {
            row.getStyleClass().remove("sub-module-row-hover");
            if (row != selectedRow) {
                arrowIcon.setVisible(false);
            }
        });

        // Click: select + launch
        row.setOnMouseClicked(e -> {
            selectRow(row, arrowIcon);
            onLaunch.accept(subModule);
        });

        return row;
    }

    private void selectRow(HBox clickedRow, FontIcon arrowIcon) {
        // Deselect previous
        if (selectedRow != null) {
            selectedRow.getStyleClass().remove("sub-module-row-selected");
            // Hide arrow on old selection if not hovered — simplest: always hide, re-show on hover
            selectedRow.getChildren().stream()
                    .filter(n -> n instanceof FontIcon)
                    .forEach(n -> n.setVisible(false));
        }

        selectedRow = clickedRow;
        selectedRow.getStyleClass().add("sub-module-row-selected");
        arrowIcon.setVisible(true);
    }
}