package com.bob.pay.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "wechat_user_sessions")
public class WechatUserSessionEntity {

    @Id
    @Column(length = 64)
    private String tokenHash;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    protected WechatUserSessionEntity() {
    }

    public WechatUserSessionEntity(String tokenHash, String userId, Instant createdAt, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String userId() {
        return userId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}
