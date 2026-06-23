package com.arits.datafast.controller.dashboard;

import com.arits.datafast.dto.automation.AutomationModuleDto;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;


public class ModuleCardController {

    private static final Logger log = LoggerFactory.getLogger(ModuleCardController.class);


    private static final String LOGO_BASE_PATH = "/com/arits/datafast/assets/img/modules/";
    private static final List<String> LOGO_EXTENSIONS = List.of(".png", ".jpg", ".jpeg");

    @FXML private Label     moduleNameLabel;
    @FXML private ImageView moduleLogoView;
    @FXML private VBox      subModuleContainer;


    private HBox selectedRow;


    public void bind(AutomationModuleDto.Module module, Consumer<AutomationModuleDto.SubModule> onLaunch) {
        moduleNameLabel.setText(module.name());
        loadModuleLogo(module.name());
        buildSubModuleRows(module.automations(), onLaunch);
    }

    // -------------------------------------------------------------------------
    // Private — logo loading
    // -------------------------------------------------------------------------


    private void loadModuleLogo(String moduleName) {
        for (String ext : LOGO_EXTENSIONS) {
            URL url = getClass().getResource(LOGO_BASE_PATH + moduleName + ext);
            if (url != null) {
                moduleLogoView.setImage(new Image(url.toExternalForm()));
                moduleLogoView.setVisible(true);
                moduleLogoView.setManaged(true);
                return;
            }
        }

        log.debug("[ModuleCard] No logo found for module '{}' under {}", moduleName, LOGO_BASE_PATH);
        moduleLogoView.setVisible(false);
        moduleLogoView.setManaged(false);
    }

    // -------------------------------------------------------------------------
    // Private — sub-module rows
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

        // 1. Create the Label
        Label nameLabel = new Label(subModule.name());
        nameLabel.getStyleClass().add("sub-module-name");

        // 2. THE FIX: Create an empty, expanding Region
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3. Create the Icon
        FontIcon arrowIcon = new FontIcon("gmi-arrow-forward");
        arrowIcon.getStyleClass().add("sub-module-arrow");
        arrowIcon.setIconSize(16);


        // 4. Add them in order: Label -> Spacer -> Icon
        row.getChildren().addAll(nameLabel, spacer, arrowIcon);

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

        row.setOnMouseClicked(e -> {
            selectRow(row, arrowIcon);
            onLaunch.accept(subModule);
        });

        return row;
    }

    private void selectRow(HBox clickedRow, FontIcon arrowIcon) {
        if (selectedRow != null) {
            selectedRow.getStyleClass().remove("sub-module-row-selected");
            selectedRow.getChildren().stream()
                    .filter(n -> n instanceof FontIcon)
                    .forEach(n -> n.setVisible(false));
        }

        selectedRow = clickedRow;
        selectedRow.getStyleClass().add("sub-module-row-selected");
        arrowIcon.setVisible(true);
    }
}