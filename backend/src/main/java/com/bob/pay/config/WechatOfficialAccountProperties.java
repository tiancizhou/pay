package com.bob.pay.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "wechat.official-account")
public class WechatOfficialAccountProperties {

    private boolean enabled;
    private String appId = "";
    private String appSecret = "";
    private String token = "";
    private String encodingAesKey = "";
    private String oauthCallbackUrl = "";
    private String oauthScope = "snsapi_base";

    @PostConstruct
    void validate() {
        if (!enabled) {
            return;
        }
        requireValue(appId, "WECHAT_MP_APP_ID");
        requireValue(appSecret, "WECHAT_MP_APP_SECRET");
        requireValue(oauthCallbackUrl, "WECHAT_MP_OAUTH_CALLBACK_URL");
        if (!"snsapi_base".equals(oauthScope) && !"snsapi_userinfo".equals(oauthScope)) {
            throw new IllegalStateException("WECHAT_MP_OAUTH_SCOPE 仅支持 snsapi_base 或 snsapi_userinfo");
        }
        var callbackUri = URI.create(oauthCallbackUrl);
        if (!"https".equalsIgnoreCase(callbackUri.getScheme()) || callbackUri.getHost() == null) {
            throw new IllegalStateException("WECHAT_MP_OAUTH_CALLBACK_URL 必须是完整的 HTTPS 地址");
        }
    }

    private void requireValue(String value, String environmentName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("公众号已启用，但未配置 " + environmentName);
        }
    }

    public PublicConfig publicConfig() {
        return new PublicConfig(enabled, enabled && isConfigured(), appId, oauthCallbackUrl, oauthScope);
    }

    private boolean isConfigured() {
        return !appId.isBlank() && !appSecret.isBlank() && !oauthCallbackUrl.isBlank();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = clean(appId);
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = clean(appSecret);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = clean(token);
    }

    public String getEncodingAesKey() {
        return encodingAesKey;
    }

    public void setEncodingAesKey(String encodingAesKey) {
        this.encodingAesKey = clean(encodingAesKey);
    }

    public String getOauthCallbackUrl() {
        return oauthCallbackUrl;
    }

    public void setOauthCallbackUrl(String oauthCallbackUrl) {
        this.oauthCallbackUrl = clean(oauthCallbackUrl);
    }

    public String getOauthScope() {
        return oauthScope;
    }

    public void setOauthScope(String oauthScope) {
        this.oauthScope = clean(oauthScope);
    }

    private String clean(String value) {
        return value == null ? "" : value.strip();
    }

    public record PublicConfig(
            boolean enabled,
            boolean configured,
            String appId,
            String oauthCallbackUrl,
            String oauthScope
    ) {
    }
}
