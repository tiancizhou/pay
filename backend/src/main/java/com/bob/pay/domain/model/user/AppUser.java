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
        String passwordHash
) {
    public AppUser withRole(UserRole nextRole, String nextTechnicianId) {
        return new AppUser(
                id,
                username,
                name,
                phone,
                nextRole,
                nextRole == UserRole.TECHNICIAN ? nextTechnicianId : null,
                passwordHash
        );
    }

    public AppUser withoutPassword() {
        return new AppUser(id, username, name, phone, role, technicianId, null);
    }
}
