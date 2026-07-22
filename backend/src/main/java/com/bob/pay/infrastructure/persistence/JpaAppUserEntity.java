package com.bob.pay.infrastructure.persistence;

import com.bob.pay.domain.model.user.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class JpaAppUserEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(length = 64)
    private String technicianId;

    @Column(unique = true, length = 64)
    private String wechatOpenId;

    @Column(length = 64)
    private String wechatUnionId;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    protected JpaAppUserEntity() {
    }

    public JpaAppUserEntity(
            String id,
            String username,
            String name,
            String phone,
            UserRole role,
            String technicianId,
            String wechatOpenId,
            String wechatUnionId,
            String passwordHash
    ) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.technicianId = technicianId;
        this.wechatOpenId = wechatOpenId;
        this.wechatUnionId = wechatUnionId;
        this.passwordHash = passwordHash;
    }

    public String id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String name() {
        return name;
    }

    public String phone() {
        return phone;
    }

    public UserRole role() {
        return role;
    }

    public String technicianId() {
        return technicianId;
    }

    public String wechatOpenId() {
        return wechatOpenId;
    }

    public String wechatUnionId() {
        return wechatUnionId;
    }

    public String passwordHash() {
        return passwordHash;
    }
}
