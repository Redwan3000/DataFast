//package com.arits.datafast.service.auth;
//
//import com.arits.datafast.dto.auth.*;
//import com.arits.datafast.service.api.ApiClient;
//import com.arits.datafast.state.AppState;
//import com.arits.datafast.util.CryptoUtil;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class AuthService {
//
//    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
//    private static final ObjectMapper MAPPER = new ObjectMapper();
//
//
//    public void authenticateUser(String email, String password) throws Exception {
//        LoginRequestDto request = new LoginRequestDto(email, password);
//        ApiClient.ApiResponse response = ApiClient.post("/api/login", MAPPER.writeValueAsString(request));
//
//        log.debug("Login response: {}", response.getBody());
//
//        log.info("Login status: {}", response.getCode());
//        log.info("Login body: {}", response.getBody());
//
//        if (!response.isSuccess()) {
//            throw new Exception("Invalid credentials. Please try again.");
//        }
//
//        AuthResponseDto data = MAPPER.readValue(response.getBody(), AuthResponseDto.class);
//
//
//        if (data.statusCode() != 200 || data.results() == null || data.results().token() == null || data.results().token().isBlank()) {
//            throw new Exception("Invalid credentials. Please try again.");
//        }
//        String token    = data.results().token();  // ← extract token here
//        String rawName  = data.results().userData().name();
//        String rawEmail = data.results().userData().email();
//        String rawPhone = data.results().userData().phoneNumber();
//
//        UserSessionData sessionData = new UserSessionData(
//                data.results().userData().id(),
//                CryptoUtil.encryptAES(rawName),
//                CryptoUtil.encryptAES(rawEmail),
//                CryptoUtil.encryptAES(rawPhone),
//                data.results().userData().role().name(),
//                data.results().userData().company().id(),
//                data.results().userData().company().name(),
//                data.results().userData().company().email(),
//                data.results().userData().company().address()
//        );
//
//        AppState.getInstance().setSession(token, response.getBody(), sessionData);
//    }
//
//
//    public void resetPassword(String email, String otp, String password) throws Exception {
//        ResetPasswordProps dto = new ResetPasswordProps(email, otp, password);
//        ApiClient.ApiResponse response = ApiClient.post("/api/forgot-reset-password", MAPPER.writeValueAsString(dto));
//
//        if (!response.isSuccess()) {
//            throw new Exception(getErrorMessage(response.getBody()));
//        }
//    }
//
//    public void requestOtp(String email) throws Exception {
//        ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto(email);
//        ApiClient.ApiResponse response = ApiClient.post("/api/forgot-password-request", MAPPER.writeValueAsString(dto));
//
//        if (!response.isSuccess()) {
//            throw new Exception(getErrorMessage(response.getBody()));
//        }
//
//        // Also check body status_code like we did for login
//        // since Laravel may return HTTP 200 with error inside
//        JsonNode json = MAPPER.readTree(response.getBody());
//        if (json.has("status_code") && json.get("status_code").asInt() != 200) {
//            throw new Exception(json.has("message")
//                    ? json.get("message").asText()
//                    : "Email not registered. Please check and try again.");
//        }
//    }
//    private String getErrorMessage(String responseBody) {
//        try {
//            JsonNode json = MAPPER.readTree(responseBody);
//            return json.has("message") ? json.get("message").asText() : "Unknown error occurred.";
//        } catch (Exception e) {
//            return "Server error: Unable to parse response.";
//        }
//    }
//}
package com.arits.datafast.service.auth;

import com.arits.datafast.dto.auth.*;
import com.arits.datafast.service.api.ApiClient;
import com.arits.datafast.service.api.ApiEndpoints;
import com.arits.datafast.state.AppState;
import com.arits.datafast.util.CryptoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    public void authenticateUser(String email, String password) throws Exception {
        LoginRequestDto request = new LoginRequestDto(email, password);
        ApiClient.ApiResponse response = ApiClient.post(
                ApiEndpoints.Auth.LOGIN,
                MAPPER.writeValueAsString(request)
        );

        log.info("[Auth] Login status: {}", response.getCode());
        log.debug("[Auth] Login body: {}", response.getBody());

        if (!response.isSuccess()) {
            throw new Exception("Invalid credentials. Please try again.");
        }

        AuthResponseDto data = MAPPER.readValue(response.getBody(), AuthResponseDto.class);

        if (data.statusCode() != 200
                || data.results() == null
                || data.results().token() == null
                || data.results().token().isBlank()) {
            throw new Exception("Invalid credentials. Please try again.");
        }

        String token    = data.results().token();
        String rawName  = data.results().userData().name();
        String rawEmail = data.results().userData().email();
        String rawPhone = data.results().userData().phoneNumber();

        UserSessionData sessionData = new UserSessionData(
                data.results().userData().id(),
                CryptoUtil.encryptAES(rawName),
                CryptoUtil.encryptAES(rawEmail),
                CryptoUtil.encryptAES(rawPhone),
                data.results().userData().role().name(),
                data.results().userData().company().id(),
                data.results().userData().company().name(),
                data.results().userData().company().email(),
                data.results().userData().company().address()
        );

        AppState.getInstance().setSession(token, response.getBody(), sessionData);
        log.info("[Auth] Session established for userId={}", sessionData.userId());
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------

    public void logout() throws Exception {
        ApiClient.ApiResponse response = ApiClient.authenticatedPost(
                ApiEndpoints.Auth.LOGOUT,
                "{}"
        );
        log.info("[Auth] Logout status: {}", response.getCode());
        // State is cleared by AppState.clear() regardless of server response.
    }

    // -------------------------------------------------------------------------
    // Forgot password — step 1: request OTP
    // -------------------------------------------------------------------------

    public void requestOtp(String email) throws Exception {
        ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto(email);
        ApiClient.ApiResponse response = ApiClient.post(
                ApiEndpoints.Auth.FORGOT_PASSWORD,
                MAPPER.writeValueAsString(dto)
        );

        log.info("[Auth] OTP request status: {}", response.getCode());

        if (!response.isSuccess()) {
            throw new Exception(extractMessage(response.getBody()));
        }

        // Laravel may return HTTP 200 with an error status_code in the body
        JsonNode json = MAPPER.readTree(response.getBody());
        if (json.has("status_code") && json.get("status_code").asInt() != 200) {
            throw new Exception(json.has("message")
                    ? json.get("message").asText()
                    : "Email not registered. Please check and try again.");
        }
    }

    // -------------------------------------------------------------------------
    // Forgot password — step 2: reset with OTP + new password
    // -------------------------------------------------------------------------

    public void resetPassword(String email, String otp, String password) throws Exception {
        ResetPasswordProps dto = new ResetPasswordProps(email, otp, password);
        ApiClient.ApiResponse response = ApiClient.post(
                ApiEndpoints.Auth.RESET_PASSWORD,
                MAPPER.writeValueAsString(dto)
        );

        log.info("[Auth] Reset password status: {}", response.getCode());

        if (!response.isSuccess()) {
            throw new Exception(extractMessage(response.getBody()));
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String extractMessage(String responseBody) {
        try {
            JsonNode json = MAPPER.readTree(responseBody);
            return json.has("message")
                    ? json.get("message").asText()
                    : "An unexpected error occurred.";
        } catch (Exception e) {
            return "Server error: unable to parse response.";
        }
    }
}