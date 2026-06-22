package com.arits.datafast.dto.automation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Top-level response wrapper for GET /api/automations/modules/list
 *
 * Example JSON:
 * {
 *   "status_code": 200,
 *   "results": [ { "id": 1, "name": "Bangladesh Bank", "automations": [...] } ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AutomationModuleDto(
        @JsonProperty("status_code") int statusCode,
        List<Module> results
) {

    /**
     * A single automation module (portal group), e.g. "Bangladesh Bank".
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Module(
            int id,
            String name,
            String description,
            List<SubModule> automations
    ) {}

    /**
     * A single automation (sub-module) listed under a module.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubModule(
            int id,
            String name,
            String description,
            @JsonProperty("moduleId") int moduleId
    ) {}
}