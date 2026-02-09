package com.rubin.insurance.policy_management_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Schema(description = "Claim details returned by the API")
public class ClaimResponse {
    @Schema(description = "Unique claim identifier")
    private Long id;
    @Schema(description = "ID of the associated policy", example = "1")
    private Long policyId;
    @Schema(description = "System-generated claim number", example = "CLM-2025-00001")
    private String claimNumber;
    @Schema(description = "Claim description", example = "Vehicle damage from collision")
    private String description;
    @Schema(description = "Claim amount", example = "5000.00")
    private BigDecimal claimAmount;
    @Schema(description = "Incident date", example = "2025-01-15")
    private LocalDate incidentDate;
    @Schema(description = "Current claim status", example = "SUBMITTED", allowableValues = {"SUBMITTED", "APPROVED", "REJECTED"})
    private String status;
    @Schema(description = "Rejection reason when status is REJECTED", example = "Documentation incomplete")
    private String rejectionReason;
    @Schema(description = "Timestamp when the claim was created")
    private Instant createdAt;
}
