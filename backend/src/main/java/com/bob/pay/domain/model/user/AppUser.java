package com.bob.pay.domain.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AppUser(
        String id,
        String username,
        String name,
        String phone,
        UserRole role,
        String technicianId,
        @JsonIgnore
        String wechatOpenId,
        @JsonIgnore
        String wechatUnionId,
        @JsonIgnore
        String passwordHash
) {
    public AppUser(
            String id,
            String username,
            String name,
            String phone,
            UserRole role,
            String technicianId,
            String passwordHash
    ) {
        this(id, username, name, phone, role, technicianId, null, null, passwordHash);
    }

    public AppUser withRole(UserRole nextRole, String nextTechnicianId) {
        return new AppUser(
                id,
                username,
                name,
                phone,
                nextRole,
                nextRole == UserRole.TECHNICIAN ? nextTechnicianId : null,
                wechatOpenId,
                wechatUnionId,
                passwordHash
        );
    }

    public AppUser withoutPassword() {
        return new AppUser(id, username, name, phone, role, technicianId, wechatOpenId, wechatUnionId, null);
    }
}
