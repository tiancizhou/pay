package com.bob.pay.interfaces.web;

import com.bob.pay.application.WechatSessionApplicationService;
import com.bob.pay.domain.model.user.AppUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@RestController
@RequestMapping("/api/client/session")
public class WechatSessionController {

    private final WechatSessionApplicationService sessionService;

    public WechatSessionController(WechatSessionApplicationService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public AppUser currentSession(HttpServletRequest request) {
        var token = request.getCookies() == null ? "" : Arrays.stream(request.getCookies())
                .filter(cookie -> WechatSessionApplicationService.COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("");
        return sessionService.authenticate(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录"));
    }
}
