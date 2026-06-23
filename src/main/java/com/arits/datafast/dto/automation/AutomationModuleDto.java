package com.arits.datafast.dto.automation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AutomationModuleDto(
        @JsonProperty("status_code") int statusCode,
        List<Module> results
) {


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Module(
            int id,
            String name,
            String description,
            List<SubModule> automations
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubModule(
            int id,
            String name,
            String description,
            @JsonProperty("moduleId") int moduleId
    ) {}
}