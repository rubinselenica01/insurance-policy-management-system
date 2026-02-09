package com.rubin.insurance.policy_management_service.configuration.exception_handling;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standard error response. For validation or business rule errors, details contains \"fieldErrors\": map of field name to list of error messages.")
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        @Schema(description = "Optional details. For validation/business rules: use key \"fieldErrors\" with value object of field name -> array of error strings")
        Map<String, Object> details
) {
}
