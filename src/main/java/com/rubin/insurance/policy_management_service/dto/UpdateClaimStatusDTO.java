package com.rubin.insurance.policy_management_service.dto;

import com.rubin.insurance.policy_management_service.model.claim.ClaimStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateClaimStatusDTO(@NotNull(message = "Claim status should not be empty")
                                ClaimStatus claimStatus,
                                   String rejectDescription) {
}
