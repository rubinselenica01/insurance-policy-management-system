package com.rubin.insurance.policy_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Insurance policy details returned by the API")
public class PolicyResponse {
    @Schema(description = "Unique policy identifier")
    private Long id;
    @Schema(description = "System-generated policy number", example = "POL-2025-00001")
    private String policyNumber;
    @Schema(description = "Policyholder full name", example = "John Smith")
    private String customerName;
    @Schema(description = "Policyholder email", example = "john.smith@example.com")
    private String customerEmail;
    @Schema(description = "Type of policy", example = "HEALTH")
    private String policyType;
    @Schema(description = "Coverage amount", example = "100000.00")
    private BigDecimal coverageAmount;
    @Schema(description = "Premium amount", example = "150.50")
    private BigDecimal premiumAmount;
    @Schema(description = "Policy start date", example = "2025-01-01")
    private LocalDate startDate;
    @Schema(description = "Policy end date", example = "2026-01-01")
    private LocalDate endDate;
    @Schema(description = "Policy status", example = "ACTIVE")
    private String status;
}
