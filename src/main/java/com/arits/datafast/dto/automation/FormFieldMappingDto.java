package com.arits.datafast.dto.automation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FormFieldMappingDto(
        @JsonProperty("status_code") int statusCode,
        List<Mapping> results
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Mapping(
            int id,
            GenericField genericField,
            String excelColumn,
            String selector,
            Company company,
            Automation automation,
            ActionType actionType
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GenericField(int id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Company(int id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Automation(int id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ActionType(int id, String name) {
    }
}