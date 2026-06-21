package com.arits.datafast.service.auth;

import com.arits.datafast.dto.auth.AuthResponseDto;
import com.arits.datafast.dto.auth.ForgotPasswordRequestDto;
import com.arits.datafast.dto.auth.LoginRequestDto;
import com.arits.datafast.dto.auth.ResetPasswordProps;
import com.arits.datafast.service.api.ApiClient;
import com.arits.datafast.state.AppState;
import com.arits.datafast.util.CryptoUtil;
import com.google.gson.Gson; // Assuming Gson, swap to Jackson's ObjectMapper if preferred
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AuthService {
    private final Gson gson = new Gson();

    public void authenticate(String email, String password) throws Exception {
        // 1. Create clean DTO
        LoginRequestDto request = new LoginRequestDto(email, password);
        String jsonPayload = gson.toJson(request);

        // 2. Make the masked API Call

        ApiClient.ApiResponse response = ApiClient.post("/api/login", jsonPayload);

// ADD THIS TEMPORARY DEBUG LINE:
        System.out.println("=== RAW SERVER RESPONSE ===");
        System.out.println(response.getBody());
        System.out.println("===========================");


        if (!response.isSuccess()) {
            AuthResponseDto errorResponse = gson.fromJson(response.getBody(), AuthResponseDto.class);
            String errorMsg = (errorResponse != null && errorResponse.message() != null)
                    ? errorResponse.message()
                    : "Login failed. Please check your credentials.";
            throw new Exception(errorMsg);
        }

        // 3. Parse success response safely
        AuthResponseDto successData = gson.fromJson(response.getBody(), AuthResponseDto.class);

        // Check if the results wrapper exists and has a token
        if (successData.results() == null || successData.results().token() == null || successData.results().token().isBlank()) {
            throw new Exception("Critical Error: No token received from server.");
        }

        // 4. Encrypt sensitive user data BEFORE storing in State
        String encryptedName = CryptoUtil.encryptAES(successData.results().userData().name());
        String encryptedEmail = CryptoUtil.encryptAES(successData.results().userData().email());

        // 5. Save to Session (State)
        AppState.getInstance().setSession(
                successData.results().token(),
                response.getBody(),
                encryptedName,
                encryptedEmail
        );
    }

    public void resetPassword(String email, String otp, String password) throws Exception {
        // 1. Prepare DTO
        ResetPasswordProps dto = new ResetPasswordProps(email, otp, password);

        // 2. Call API
        var response = ApiClient.post("/api/forgot-reset-password", new Gson().toJson(dto));

        if (!response.isSuccess()) {
            throw new Exception("Reset failed: " + response.getBody());
        }
    }


    public ApiClient.ApiResponse requestOtp(String email) throws Exception {
        // Create the DTO
        ForgotPasswordRequestDto dto = new ForgotPasswordRequestDto(email);
        String jsonPayload = gson.toJson(dto);

        // Make the call
        return ApiClient.post("/api/forgot-password-request", jsonPayload);
    }

    public String getErrorMessage(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            return json.has("message") ? json.get("message").getAsString() : "Unknown error occurred.";
        } catch (Exception e) {
            return "Server error: Unable to parse response.";
        }
    }
}