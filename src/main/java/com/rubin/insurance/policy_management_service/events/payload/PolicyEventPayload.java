package com.rubin.insurance.policy_management_service.events.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rubin.insurance.policy_management_service.model.policy.Policy;
import com.rubin.insurance.policy_management_service.model.policy.PolicyStatus;
import com.rubin.insurance.policy_management_service.model.policy.PolicyType;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PolicyEventPayload {
    Long policyId;
    String policyNumber;
    PolicyStatus status;
    PolicyType policyType;
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal coverageAmount;
    BigDecimal premiumAmount;

    public static PolicyEventPayload buildPayload(Policy policy) {
        return PolicyEventPayload.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .status(policy.getStatus())
                .policyType(policy.getPolicyType())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .coverageAmount(policy.getCoverageAmount())
                .premiumAmount(policy.getPremiumAmount())
                .build();
    }
}
