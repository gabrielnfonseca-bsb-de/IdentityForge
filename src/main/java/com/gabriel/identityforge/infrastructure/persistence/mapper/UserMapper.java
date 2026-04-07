package com.gabriel.identityforge.infrastructure.persistence.mapper;

import com.gabriel.identityforge.domain.model.Permission;
import com.gabriel.identityforge.domain.model.Role;
import com.gabriel.identityforge.domain.model.User;
import com.gabriel.identityforge.infrastructure.persistence.entity.UserEntity;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        User user = new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getTenantId()
        );

        // roles + permissions
        if (entity.getRoles() != null) {
            entity.getRoles().forEach(roleEntity -> {

                Role role = new Role(
                        roleEntity.getId(),
                        roleEntity.getName(),
                        roleEntity.getDescription()
                );

                if (roleEntity.getPermissions() != null) {
                    roleEntity.getPermissions().forEach(permissionEntity ->
                            role.addPermission(
                                    new Permission(
                                            permissionEntity.getId(),
                                            permissionEntity.getName(),
                                            permissionEntity.getDescription()
                                    )
                            )
                    );
                }

                user.addRole(role);
            });
        }

        return user;
    }

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setTenantId(user.getTenantId());
        entity.setStatus(user.isActive() ? "ACTIVE" : "INACTIVE");

        return entity;
    }
}

