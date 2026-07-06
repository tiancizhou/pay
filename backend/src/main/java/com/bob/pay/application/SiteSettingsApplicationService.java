package com.bob.pay.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SiteSettingsApplicationService {

    private static final SiteSettings DEFAULT_SETTINGS = new SiteSettings("18069906336");

    private final Path settingsFile;
    private final ObjectMapper objectMapper;

    public SiteSettingsApplicationService(
            @Value("${app.upload-dir:${user.dir}/uploads}") String uploadDir,
            ObjectMapper objectMapper
    ) {
        this.settingsFile = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("site-settings.json");
        this.objectMapper = objectMapper;
    }

    public synchronized SiteSettings getSettings() {
        if (!Files.exists(settingsFile)) {
            return DEFAULT_SETTINGS;
        }
        try {
            return sanitize(objectMapper.readValue(settingsFile.toFile(), SiteSettings.class));
        } catch (IOException exception) {
            throw new IllegalStateException("站点配置读取失败", exception);
        }
    }

    public synchronized SiteSettings saveSettings(SiteSettings command) {
        var next = sanitize(command);
        try {
            Files.createDirectories(settingsFile.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile.toFile(), next);
            return next;
        } catch (IOException exception) {
            throw new IllegalStateException("站点配置保存失败", exception);
        }
    }

    private SiteSettings sanitize(SiteSettings command) {
        var phone = command == null ? "" : command.merchantServicePhone();
        phone = phone == null ? "" : phone.strip();
        if (phone.isBlank()) {
            return DEFAULT_SETTINGS;
        }
        if (!phone.matches("[0-9+()\\-\\s]{5,30}")) {
            throw new IllegalArgumentException("客服电话格式不正确");
        }
        return new SiteSettings(phone);
    }

    public record SiteSettings(String merchantServicePhone) {
    }
}
