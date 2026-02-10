package com.rubin.insurance.policy_management_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper with status, message, and optional data payload")
public record ApiResponseDTO<T>(
        @Schema(description = "Response status", example = "SUCCESS", allowableValues = {"SUCCESS", "ERROR"})
        String status,
        @Schema(description = "Human-readable message describing the response", example = "Policy created successfully")
        String message,
        @Schema(description = "Optional error code for error responses")
        Integer errorCode,
        @Schema(description = "ISO formatted timestamp when the response was created", example = "2025-02-09T10:30:00")
        String timestamp,
        @Schema(description = "Response payload (policy, claim, list, or pagination data)",
                oneOf = {PolicyResponse.class, ClaimResponse.class, PageResponse.class})
        @JsonInclude(JsonInclude.Include.ALWAYS)
        T data
) {
    public ApiResponseDTO(String status, String message, T data) {
        this(status, message, null, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), data);
    }

    public ApiResponseDTO(String status, String message, Integer errorCode, T data) {
        this(status, message, errorCode, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), data);
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>("SUCCESS", message, data);
    }
}
