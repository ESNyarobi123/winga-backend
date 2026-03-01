package com.winga.dto.response;

/**
 * After upload: URL path to access the file (e.g. /uploads/profile/xxx.jpg).
 * Frontend can prepend API base URL if needed: baseUrl + url
 */
public record UploadResponse(String url) {}
