package com.rubin.insurance.policy_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request body for submitting a new claim")
public record ClaimRequest(
        @NotNull(message = "Policy Id should be present")
        @Schema(description = "ID of the policy this claim belongs to", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long policyId,

        @NotBlank(message = "Description should not be empty")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Description of the claim incident (max 500 characters)", example = "Vehicle damage from collision on highway", requiredMode = Schema.RequiredMode.REQUIRED)
        String description,

        @NotNull(message = "Claim amount should not be empty")
        @Positive(message = "Claim amount should be positive number")
        @Schema(description = "Claim amount requested", example = "5000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal claimAmount,

        @NotNull(message = "Incident Date cannot be empty!")
        @PastOrPresent(message = "Incident Date should not be in the future")
        @Schema(description = "Date when the incident occurred (YYYY-MM-DD)", example = "2025-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate incidentDate
) {}
