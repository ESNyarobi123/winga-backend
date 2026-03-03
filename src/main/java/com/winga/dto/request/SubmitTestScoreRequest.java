package com.winga.dto.request;

import jakarta.validation.constraints.NotNull;

public record SubmitTestScoreRequest(@NotNull Integer score) {}
