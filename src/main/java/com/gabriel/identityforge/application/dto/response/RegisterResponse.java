package com.gabriel.identityforge.application.dto.response;

import java.util.UUID;

public class RegisterResponse {
    private UUID id;
    private String email;

    public RegisterResponse(UUID id, String email) {
        this.id = id;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
