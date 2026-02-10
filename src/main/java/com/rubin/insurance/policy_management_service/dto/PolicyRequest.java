package com.rubin.insurance.policy_management_service.dto;

import com.rubin.insurance.policy_management_service.dto.validators.CustomerFullName;
import com.rubin.insurance.policy_management_service.dto.validators.Email;
import com.rubin.insurance.policy_management_service.dto.validators.PremiumAmount;
import com.rubin.insurance.policy_management_service.dto.validators.ValidDateRange;
import com.rubin.insurance.policy_management_service.model.policy.PolicyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@PremiumAmount(coverageAmount = "coverageAmount", premiumAmount = "premiumAmount")
@ValidDateRange
@Schema(description = "Request body for creating a new insurance policy")
public record PolicyRequest(
        @NotBlank(message = "Customer name should not be empty")
        @CustomerFullName
        @Schema(description = "Full name of the policyholder", example = "John Smith", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerName,

        @NotBlank(message = "Customer email should not be empty")
        @Email
        @Schema(description = "Email address of the policyholder", example = "john.smith@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerEmail,

        @NotNull(message = "Policy type should not be empty")
        @Schema(description = "Type of insurance policy", example = "HEALTH", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"HEALTH", "AUTO", "HOME", "LIFE"})
        PolicyType policyType,

        @NotNull(message = "Coverage Amount should not be empty")
        @Positive(message = "Coverage Amount should be a positive value")
        @Schema(description = "Maximum coverage amount in currency units", example = "100000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal coverageAmount,

        @NotNull(message = "Premium Amount should not be empty")
        @Positive(message = "Premium Amount should be a positive value")
        @Schema(description = "Premium amount to be paid", example = "150.50", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal premiumAmount,

        @NotNull(message = "Start Date should not be empty")
        @Schema(description = "Policy start date (YYYY-MM-DD)", example = "2025-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate startDate,

        @NotNull(message = "End Date should not be empty")
        @Schema(description = "Policy end date (YYYY-MM-DD). Must be after start date.", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate endDate
) {}
