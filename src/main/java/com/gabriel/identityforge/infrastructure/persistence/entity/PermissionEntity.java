package com.gabriel.identityforge.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    public PermissionEntity() {
    }
    public PermissionEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {}
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {this.description = description;}

    @Override
    public String toString() {
        return "";
    }
}
