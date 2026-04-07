package com.gabriel.identityforge.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {

    // ==========================
    // Atributos
    // ==========================
    private final UUID id;
    private final String email;
    private String passwordHash;
    private String status;
    private final UUID tenantId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;


    private final Set<Role> roles = new HashSet<>();

    // ==========================
    // Construtor de RESTORE (banco)
    // ==========================
    public User(
            UUID id,
            String email,
            String passwordHash,
            UUID tenantId,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime lastLogin
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.tenantId = tenantId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
    }

    // ==========================
    // Construtor de CREATE (novo usuário)
    // ==========================
    public User(UUID id, String email, String passwordHash, UUID tenantId) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.tenantId = tenantId;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    // ==========================
    // Regras de negócio
    // ==========================

    public void addRole(Role role) {
        roles.add(role);
    }

    public boolean hasPermission(String permissionName) {
        return roles.stream()
                .anyMatch(role -> role.hasPermission(permissionName));
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void changePassword(String newHash) {
        this.passwordHash = newHash;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = "INACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    // ==========================
    // Getters
    // ==========================

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

}

