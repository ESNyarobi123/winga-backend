package com.winga.dto.response;

import java.util.List;

/**
 * All filter options for find-jobs page, grouped by type (Employment Type, Social Media, Software, Languages).
 */
public record FilterOptionsPublicResponse(
        List<FilterOptionResponse> employmentTypes,
        List<FilterOptionResponse> socialMedia,
        List<FilterOptionResponse> software,
        List<FilterOptionResponse> languages
) {}
