package com.arits.datafast.service.automation;

import com.arits.datafast.state.FieldMappingRow;

import java.util.*;

public class ColumnMatcher {

    /**
     * Fills in selectedExcelColumn for any row that isn't already mapped.
     */
    public void autoMatch(List<FieldMappingRow> rows, List<String> excelHeaders) {
        Map<String, String> normalizedHeaders = new LinkedHashMap<>();
        for (String header : excelHeaders) {
            normalizedHeaders.putIfAbsent(normalize(header), header);
        }

        Set<String> usedHeaders = new HashSet<>();
        for (FieldMappingRow row : rows) {
            if (row.isMapped()) usedHeaders.add(row.getSelectedExcelColumn());
        }

        for (FieldMappingRow row : rows) {
            if (row.isMapped()) continue; // don't clobber a manual pick

            String seed = (row.getDefaultExcelColumn() != null && !row.getDefaultExcelColumn().isBlank())
                    ? row.getDefaultExcelColumn()
                    : row.getFieldName();
            String normalizedSeed = normalize(seed);

            String match = normalizedHeaders.get(normalizedSeed);
            if (match == null) {
                for (Map.Entry<String, String> entry : normalizedHeaders.entrySet()) {
                    if (usedHeaders.contains(entry.getValue())) continue;
                    if (entry.getKey().contains(normalizedSeed) || normalizedSeed.contains(entry.getKey())) {
                        match = entry.getValue();
                        break;
                    }
                }
            }

            if (match != null && !usedHeaders.contains(match)) {
                row.setSelectedExcelColumn(match);
                usedHeaders.add(match);
            }
        }
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}