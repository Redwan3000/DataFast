package com.arits.datafast.service.api;

import com.arits.datafast.config.AppConfig;
import com.arits.datafast.state.AppState;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = AppConfig.getAppConfig().getString("api.base.url");
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();


    public static ApiResponse post(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return new ApiResponse(response.statusCode(), response.body());
    }


    public static ApiResponse authenticatedGet(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(TIMEOUT)
                .header("Authorization", requireToken())
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return new ApiResponse(response.statusCode(), response.body());
    }


    public static ApiResponse authenticatedPost(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(TIMEOUT)
                .header("Authorization", requireToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return new ApiResponse(response.statusCode(), response.body());
    }


    private static String requireToken() {
        String token = AppState.getAppState().getAuthToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("ApiClient: No auth token — must be logged in first");
        }
        return token;
    }


    public static class ApiResponse {
        private final int statusCode;
        private final String body;

        public ApiResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        @Override
        public String toString() {
            return "ApiResponse{status=" + statusCode + ", body='" + body + "'}";
        }
    }
}