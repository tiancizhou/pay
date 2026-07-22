package com.bob.pay.interfaces.web;

import com.bob.pay.application.WechatOAuthApplicationService;
import com.bob.pay.application.WechatSessionApplicationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

@RestController
@RequestMapping("/api/wechat/oauth")
public class WechatOAuthController {

    private static final String STATE_COOKIE = "wechat_oauth_state";
    private static final String RETURN_TO_COOKIE = "wechat_oauth_return_to";
    private static final Duration OAUTH_COOKIE_DURATION = Duration.ofMinutes(5);

    private final SecureRandom secureRandom = new SecureRandom();
    private final WechatOAuthApplicationService oauthService;
    private final WechatSessionApplicationService sessionService;

    public WechatOAuthController(
            WechatOAuthApplicationService oauthService,
            WechatSessionApplicationService sessionService
    ) {
        this.oauthService = oauthService;
        this.sessionService = sessionService;
    }

    @GetMapping("/start")
    public ResponseEntity<Void> start(
            @RequestParam(defaultValue = "/client") String returnTo,
            HttpServletResponse response
    ) {
        var safeReturnTo = safeReturnTo(returnTo);
        var state = randomState();
        addCookie(response, STATE_COOKIE, state, "/api/wechat/oauth", OAUTH_COOKIE_DURATION);
        addCookie(response, RETURN_TO_COOKIE, encode(safeReturnTo), "/api/wechat/oauth", OAUTH_COOKIE_DURATION);
        return redirect(oauthService.authorizationUri(state));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var expectedState = cookieValue(request, STATE_COOKIE);
        if (!secureEquals(expectedState, state)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "微信授权状态已失效，请重新进入");
        }

        var returnTo = safeReturnTo(decode(cookieValue(request, RETURN_TO_COOKIE)));
        clearCookie(response, STATE_COOKIE, "/api/wechat/oauth");
        clearCookie(response, RETURN_TO_COOKIE, "/api/wechat/oauth");

        var user = oauthService.authenticate(code);
        var session = sessionService.createSession(user.id());
        addCookie(response, WechatSessionApplicationService.COOKIE_NAME, session.value(), "/", session.duration());
        return redirect(URI.create(returnTo));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        sessionService.revoke(cookieValue(request, WechatSessionApplicationService.COOKIE_NAME));
        clearCookie(response, WechatSessionApplicationService.COOKIE_NAME, "/");
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Void> redirect(URI location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .cacheControl(CacheControl.noStore())
                .build();
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, Duration maxAge) {
        var cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        addCookie(response, name, "", path, Duration.ZERO);
    }

    private String cookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return "";
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("");
    }

    private String safeReturnTo(String returnTo) {
        if (returnTo == null || !returnTo.startsWith("/") || returnTo.startsWith("//") || returnTo.length() > 300) {
            return "/client";
        }
        return returnTo;
    }

    private String randomState() {
        var bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean secureEquals(String expected, String actual) {
        if (expected == null || expected.isBlank() || actual == null || actual.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return "/client";
        }
    }
}
