package com.bob.pay.application;

import com.bob.pay.domain.model.user.AppUser;
import com.bob.pay.domain.model.user.AppUserRepository;
import com.bob.pay.infrastructure.persistence.WechatUserSessionEntity;
import com.bob.pay.infrastructure.persistence.WechatUserSessionJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class WechatSessionApplicationService {

    public static final String COOKIE_NAME = "dao_home_session";
    public static final Duration SESSION_DURATION = Duration.ofDays(30);

    private final SecureRandom secureRandom = new SecureRandom();
    private final WechatUserSessionJpaRepository sessionRepository;
    private final AppUserRepository userRepository;

    public WechatSessionApplicationService(
            WechatUserSessionJpaRepository sessionRepository,
            AppUserRepository userRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SessionToken createSession(String userId) {
        sessionRepository.deleteByExpiresAtBefore(Instant.now());
        var token = randomToken();
        var now = Instant.now();
        sessionRepository.save(new WechatUserSessionEntity(
                hash(token),
                userId,
                now,
                now.plus(SESSION_DURATION)
        ));
        return new SessionToken(token, SESSION_DURATION);
    }

    @Transactional
    public Optional<AppUser> authenticate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        var session = sessionRepository.findById(hash(token));
        if (session.isEmpty()) {
            return Optional.empty();
        }
        if (!session.get().expiresAt().isAfter(Instant.now())) {
            sessionRepository.delete(session.get());
            return Optional.empty();
        }
        return userRepository.findById(session.get().userId()).map(AppUser::withoutPassword);
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            sessionRepository.deleteById(hash(token));
        }
    }

    private String randomToken() {
        var bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }

    public record SessionToken(String value, Duration duration) {
    }
}
