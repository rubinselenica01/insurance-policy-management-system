package com.rubin.insurance.policy_management_service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Insurance policy details returned by the API")
public record PolicyResponse(
        @Schema(description = "Unique policy identifier")
        Long id,
        @Schema(description = "System-generated policy number", example = "POL-2025-00001")
        String policyNumber,
        @Schema(description = "Policyholder full name", example = "John Smith")
        String customerName,
        @Schema(description = "Policyholder email", example = "john.smith@example.com")
        String customerEmail,
        @Schema(description = "Type of policy", example = "HEALTH")
        String policyType,
        @Schema(description = "Coverage amount", example = "100000.00")
        BigDecimal coverageAmount,
        @Schema(description = "Premium amount", example = "150.50")
        BigDecimal premiumAmount,
        @Schema(description = "Policy start date", example = "2025-01-01")
        LocalDate startDate,
        @Schema(description = "Policy end date", example = "2026-01-01")
        LocalDate endDate,
        @Schema(description = "Policy status", example = "ACTIVE")
        String policyStatus
) {}
