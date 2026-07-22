package com.bob.pay.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bob.pay.config.WechatOfficialAccountProperties;
import com.bob.pay.domain.model.user.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class WechatOAuthApplicationService {

    private static final Logger log = LoggerFactory.getLogger(WechatOAuthApplicationService.class);
    private static final String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";

    private final WechatOfficialAccountProperties properties;
    private final UserApplicationService userApplicationService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WechatOAuthApplicationService(
            WechatOfficialAccountProperties properties,
            UserApplicationService userApplicationService,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.userApplicationService = userApplicationService;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public URI authorizationUri(String state) {
        ensureEnabled();
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL +
                        "?appid={appid}&redirect_uri={redirectUri}&response_type=code&scope={scope}&state={state}")
                .fragment("wechat_redirect")
                .encode()
                .buildAndExpand(
                        properties.getAppId(),
                        properties.getOauthCallbackUrl(),
                        properties.getOauthScope(),
                        state
                )
                .toUri();
    }

    public AppUser authenticate(String code) {
        ensureEnabled();
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "微信授权未返回有效 code");
        }

        WechatAccessTokenResponse response;
        try {
            var responseBody = restClient.get()
                    .uri(UriComponentsBuilder.fromUriString(ACCESS_TOKEN_URL)
                            .queryParam("appid", properties.getAppId())
                            .queryParam("secret", properties.getAppSecret())
                            .queryParam("code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build()
                            .encode()
                            .toUri())
                    .retrieve()
                    .body(String.class);
            response = parseAccessTokenResponse(responseBody);
        } catch (RuntimeException exception) {
            log.warn("WeChat OAuth token exchange raised {}", exception.getClass().getName());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "连接微信授权服务失败", exception);
        }

        if (response == null || response.errcode() != null || response.openid() == null || response.openid().isBlank()) {
            var errorCode = response == null || response.errcode() == null ? "unknown" : response.errcode().toString();
            var errorMessage = response == null || response.errmsg() == null ? "empty response" : response.errmsg();
            log.warn("WeChat OAuth token exchange failed: errcode={}, errmsg={}", errorCode, errorMessage);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "微信授权失败，错误码：" + errorCode);
        }
        return userApplicationService.findOrCreateWechatUser(response.openid(), response.unionid());
    }

    private WechatAccessTokenResponse parseAccessTokenResponse(String responseBody) {
        try {
            JsonNode payload = objectMapper.readTree(responseBody);
            return new WechatAccessTokenResponse(
                    textValue(payload, "openid"),
                    textValue(payload, "unionid"),
                    payload.hasNonNull("errcode") ? payload.get("errcode").asInt() : null,
                    textValue(payload, "errmsg")
            );
        } catch (Exception exception) {
            throw new IllegalStateException("无法解析微信授权响应", exception);
        }
    }

    private String textValue(JsonNode payload, String field) {
        return payload.hasNonNull(field) ? payload.get(field).asText() : null;
    }

    private void ensureEnabled() {
        if (!properties.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "微信公众号登录尚未启用");
        }
    }

    private record WechatAccessTokenResponse(
            String openid,
            String unionid,
            Integer errcode,
            String errmsg
    ) {
    }
}
