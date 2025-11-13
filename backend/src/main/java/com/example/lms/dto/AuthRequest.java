
package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request containing Azure AD access token")
public class AuthRequest {

    @Schema(description = "Azure AD access token", required = true, example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJS...")
    private String accessToken;

    // Default constructor
    public AuthRequest() {
    }

    // Constructor with parameters
    public AuthRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getter
    public String getAccessToken() {
        return accessToken;
    }

    // Setter
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
