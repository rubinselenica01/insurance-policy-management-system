package com.rubin.insurance.policy_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Claim details returned by the API")
public record ClaimResponse(
        @Schema(description = "Unique claim identifier")
        Long id,
        @Schema(description = "ID of the associated policy", example = "1")
        Long policyId,
        @Schema(description = "System-generated claim number", example = "CLM-2025-00001")
        String claimNumber,
        @Schema(description = "Claim description", example = "Vehicle damage from collision")
        String description,
        @Schema(description = "Claim amount", example = "5000.00")
        BigDecimal claimAmount,
        @Schema(description = "Incident date", example = "2025-01-15")
        LocalDate incidentDate,
        @Schema(description = "Current claim status", example = "SUBMITTED", allowableValues = {"SUBMITTED", "APPROVED", "REJECTED"})
        String status,
        @Schema(description = "Rejection reason when status is REJECTED", example = "Documentation incomplete")
        String rejectionReason,
        @Schema(description = "Timestamp when the claim was created")
        Instant createdAt
) {}
