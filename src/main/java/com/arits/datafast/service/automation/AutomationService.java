package com.arits.datafast.service.automation;

import com.arits.datafast.dto.automation.AutomationModuleDto;
import com.arits.datafast.service.api.ApiClient;
import com.arits.datafast.service.api.ApiEndpoints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;


public class AutomationService {

    private static final Logger log = LoggerFactory.getLogger(AutomationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();


    public List<AutomationModuleDto.Module> fetchModules() throws Exception {
        ApiClient.ApiResponse response = ApiClient.authenticatedGet(ApiEndpoints.Modules.LIST);

        log.debug("Modules list response [{}]: {}", response.getCode(), response.getBody());

        if (!response.isSuccess()) {
            throw new Exception("Failed to load modules. Server returned: " + response.getCode());
        }

        AutomationModuleDto dto = MAPPER.readValue(response.getBody(), AutomationModuleDto.class);

        if (dto.statusCode() != 200 || dto.results() == null) {
            throw new Exception("Unexpected response from server while loading modules.");
        }

        return Collections.unmodifiableList(dto.results());
    }
}