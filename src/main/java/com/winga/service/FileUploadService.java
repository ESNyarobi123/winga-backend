package com.winga.service;

import com.winga.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Local file upload: store under uploads/{type}/ and return URL path for serving.
 * Use type=profile for profile picture, type=cv for CV document (PDF). Admin can view via GET /uploads/**.
 */
@Service
@Slf4j
public class FileUploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif", "pdf");

    /** Allowed folder names: profile (picture), cv (document), job, proposal, general */
    private static final Set<String> ALLOWED_FOLDER_TYPES = Set.of("profile", "cv", "job", "proposal", "general");

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size-bytes:10485760}")
    private long maxFileSizeBytes;

    @Value("${app.upload.base-url:}")
    private String baseUrl;

    /**
     * Save multipart file under uploads/{type}/ and return path (or full URL if app.upload.base-url is set).
     * type: profile (picture), cv (PDF), job, proposal, general.
     */
    public String upload(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("No file provided.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("File type not allowed. Use: image (jpeg, png, webp, gif) or PDF.");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException("File too large. Max size: " + (maxFileSizeBytes / 1_048_576) + " MB.");
        }
        String safeType = type != null && ALLOWED_FOLDER_TYPES.contains(type.toLowerCase()) ? type.toLowerCase() : "general";
        String ext = getExtension(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException("File extension not allowed.");
        }
        String filename = UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(uploadDir, safeType);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            String path = "/uploads/" + safeType + "/" + filename;
            log.debug("Uploaded: {}", path);
            if (baseUrl != null && !baseUrl.isBlank()) {
                String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                return base + path;
            }
            return path;
        } catch (IOException e) {
            log.error("Upload failed: {}", e.getMessage());
            throw new BusinessException("Failed to save file: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) return null;
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i + 1) : null;
    }
}
