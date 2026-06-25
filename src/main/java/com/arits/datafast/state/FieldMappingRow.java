package com.arits.datafast.state;

public class FieldMappingRow {

    private final int genericFieldId;
    private final String fieldName;
    private final String defaultExcelColumn;
    private final String actionType;
    private final String selector;

    private String selectedExcelColumn;

    public FieldMappingRow(int genericFieldId, String fieldName, String defaultExcelColumn,
                           String actionType, String selector) {
        this.genericFieldId = genericFieldId;
        this.fieldName = fieldName;
        this.defaultExcelColumn = defaultExcelColumn;
        this.actionType = actionType;
        this.selector = selector;
    }

    public boolean isMapped() {
        return selectedExcelColumn != null && !selectedExcelColumn.isBlank();
    }

    public int getGenericFieldId() {
        return genericFieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDefaultExcelColumn() {
        return defaultExcelColumn;
    }

    public String getActionType() {
        return actionType;
    }

    public String getSelector() {
        return selector;
    }

    public String getSelectedExcelColumn() {
        return selectedExcelColumn;
    }

    public void setSelectedExcelColumn(String selectedExcelColumn) {
        this.selectedExcelColumn = selectedExcelColumn;
    }
}