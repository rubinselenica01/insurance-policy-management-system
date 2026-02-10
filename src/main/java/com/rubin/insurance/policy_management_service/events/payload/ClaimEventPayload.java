package com.rubin.insurance.policy_management_service.events.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rubin.insurance.policy_management_service.model.claim.Claim;
import com.rubin.insurance.policy_management_service.model.claim.ClaimStatus;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClaimEventPayload {
    Long claimId;
    String claimNumber;
    Long policyId;
    ClaimStatus status;
    BigDecimal claimAmount;
    LocalDate incidentDate;
    String rejectionReason;

    public static ClaimEventPayload buildPayload(Claim claim) {
        return ClaimEventPayload.builder()
                .claimId(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicy().getId())
                .status(claim.getStatus())
                .claimAmount(claim.getClaimAmount())
                .incidentDate(claim.getIncidentDate())
                .rejectionReason(claim.getRejectionReason())
                .build();
    }
}
