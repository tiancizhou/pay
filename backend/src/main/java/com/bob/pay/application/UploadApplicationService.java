package com.bob.pay.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadApplicationService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path uploadRoot;

    public UploadApplicationService(@Value("${app.upload-dir:${user.dir}/uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public UploadedFile saveServiceCover(MultipartFile file) {
        return saveImage(file, "service-covers");
    }

    public UploadedFile saveTechnicianAvatar(MultipartFile file) {
        return saveImage(file, "technician-avatars");
    }

    private UploadedFile saveImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("图片不能超过 5MB");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、WebP 图片");
        }

        String extension = extensionFor(contentType);
        String filename = UUID.randomUUID() + "." + extension;
        Path directory = uploadRoot.resolve(folder).normalize();
        Path target = directory.resolve(filename).normalize();
        if (!target.startsWith(directory)) {
            throw new IllegalArgumentException("文件路径不合法");
        }

        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("图片保存失败", ex);
        }

        return new UploadedFile("/uploads/" + folder + "/" + filename);
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    public record UploadedFile(String url) {
    }
}
