package com.rubin.insurance.policy_management_service.events.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rubin.insurance.policy_management_service.model.claim.Claim;
import com.rubin.insurance.policy_management_service.model.claim.ClaimStatus;

public record ClaimEventPayload(
    Long claimId,
    String claimNumber,
    Long policyId,
    String customerEmail,
    ClaimStatus status,
    BigDecimal claimAmount,
    LocalDate incidentDate,
    String rejectionReason
) {
    public static ClaimEventPayload buildPayload(Claim claim) {
        return new ClaimEventPayload(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getPolicy().getId(),
                claim.getPolicy().getCustomerEmail(),
                claim.getStatus(),
                claim.getClaimAmount(),
                claim.getIncidentDate(),
                claim.getRejectionReason()
        );
    }
}
