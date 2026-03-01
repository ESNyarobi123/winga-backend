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
 */
@Service
@Slf4j
public class FileUploadService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif", "pdf");

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size-bytes:10485760}")
    private long maxFileSizeBytes;

    /**
     * Save multipart file under uploads/{type}/ and return path like /uploads/profile/xxx.jpg.
     * type: profile, job, proposal, or general.
     */
    public String upload(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("No file provided.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("File type not allowed. Use: image (jpeg, png, webp, gif) or PDF.");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException("File too large. Max size: " + (maxFileSizeBytes / 1_048_576) + " MB.");
        }
        String safeType = type != null && type.matches("^[a-z]+$") ? type : "general";
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
            // Return path that the resource handler will serve (e.g. /uploads/profile/xxx.jpg)
            String path = "/uploads/" + safeType + "/" + filename;
            log.debug("Uploaded: {}", path);
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
