package com.gabriel.identityforge.application.dto.request;

import java.util.UUID;

public class RegisterRequest {
    private String email;
    private String password;
    private UUID tenantId;

    public RegisterRequest() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
}
