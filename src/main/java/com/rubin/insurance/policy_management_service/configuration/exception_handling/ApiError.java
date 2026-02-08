package com.rubin.insurance.policy_management_service.configuration.exception_handling;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        Map<String, Object> details
) {
}
