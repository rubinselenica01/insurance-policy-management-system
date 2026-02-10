package com.rubin.insurance.policy_management_service.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object for standardized API responses.
 *
 * This class provides a consistent structure for all API responses in the application,
 * with support for success and error responses, error codes, and a generic data payload.
 * It includes static factory methods to create common response types.
 *
 * @param <T> the type of data payload contained in the response
 *
 * @author SuperAnalyst Team
 * @since 1.0
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper with status, message, and optional data payload")
public class ApiResponseDTO<T> {
    @Schema(description = "Response status", example = "SUCCESS", allowableValues = {"SUCCESS", "ERROR"})
    private String status;

    @Schema(description = "Human-readable message describing the response", example = "Policy created successfully")
    private String message;

    @Schema(description = "Optional error code for error responses")
    private Integer errorCode;

    @Schema(description = "ISO formatted timestamp when the response was created", example = "2025-02-09T10:30:00")
    private String timestamp;

    @Schema(description = "Response payload (policy, claim, list, or pagination data)",
            oneOf = {PolicyResponse.class, ClaimResponse.class, PageResponse.class})
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private T data;

    public ApiResponseDTO(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public ApiResponseDTO(String status, String message, Integer errorCode, T data) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>("SUCCESS", message, data);
    }



}