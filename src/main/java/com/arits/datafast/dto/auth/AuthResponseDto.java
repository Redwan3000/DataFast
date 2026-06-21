package com.arits.datafast.dto.auth;

public record AuthResponseDto(int status_code, Results results, String message) {

    // The wrapper object Laravel uses
    public record Results(String token, UserData userData) {}

    // The actual user data inside the wrapper
    public record UserData(String name, String email) {}
}