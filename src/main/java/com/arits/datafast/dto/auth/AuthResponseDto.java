package com.arits.datafast.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponseDto(
        @JsonProperty("status_code") int statusCode,
        String message,
        Results results
) {
    public record Results(
            String token,
            UserData userData
    ) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserData(
            int    id,
            String name,
            String email,
            String phoneNumber,
            Role   role,
            Company company
    ) {}

    public record Role(
            int    id,
            String name
    ) {}

    public record Company(
            int    id,
            String name,
            String email,
            String address
    ) {}
}