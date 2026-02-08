package com.rubin.insurance.policy_management_service.model.claim;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.rubin.insurance.policy_management_service.configuration.exception_handling.BadRequestException;
import com.rubin.insurance.policy_management_service.model.policy.PolicyType;

import java.util.Arrays;

public enum ClaimStatus {
    SUBMITTED,
    APPROVED,
    REJECTED;

    @JsonCreator
    public static ClaimStatus fromValue(String v) {
        return Arrays.stream(ClaimStatus.values())
                .filter(el -> el.name().equalsIgnoreCase(v))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        String.format("Claim Status should be amongst : %s", Arrays.toString(ClaimStatus.values()))
                ));
    }
}
