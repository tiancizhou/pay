package com.bob.pay.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAppUserJpaRepository extends JpaRepository<JpaAppUserEntity, String> {

    Optional<JpaAppUserEntity> findByUsername(String username);

    Optional<JpaAppUserEntity> findByWechatOpenId(String wechatOpenId);
}
