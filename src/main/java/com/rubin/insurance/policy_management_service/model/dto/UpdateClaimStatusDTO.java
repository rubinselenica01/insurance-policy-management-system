package com.rubin.insurance.policy_management_service.model.dto;

import com.rubin.insurance.policy_management_service.model.entity.claim.ClaimStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for updating a claim's status")
public record UpdateClaimStatusDTO(
        @NotNull(message = "Claim status should not be empty")
        @Schema(description = "New status for the claim", example = "APPROVED", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"SUBMITTED", "APPROVED", "REJECTED"})
        ClaimStatus claimStatus,
        @Schema(description = "Reason for rejection (required when status is REJECTED)", example = "Documentation incomplete")
        String rejectDescription) {
}
