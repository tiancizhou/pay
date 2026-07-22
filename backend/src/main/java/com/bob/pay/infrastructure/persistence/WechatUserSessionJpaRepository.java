package com.bob.pay.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface WechatUserSessionJpaRepository extends JpaRepository<WechatUserSessionEntity, String> {

    void deleteByExpiresAtBefore(Instant expiresAt);
}
