package com.rubin.insurance.policy_management_service.messaging.events.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rubin.insurance.policy_management_service.model.entity.policy.Policy;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyStatus;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyType;

import lombok.Builder;

@Builder
public record PolicyEventPayload(Long policyId, String policyNumber, String customerEmail, PolicyStatus status,
                                 PolicyType policyType, LocalDate startDate, LocalDate endDate,
                                 BigDecimal coverageAmount, BigDecimal premiumAmount) {
    public static PolicyEventPayload buildPayload(Policy policy) {
        return PolicyEventPayload.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerEmail(policy.getCustomerEmail())
                .status(policy.getStatus())
                .policyType(policy.getPolicyType())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .coverageAmount(policy.getCoverageAmount())
                .premiumAmount(policy.getPremiumAmount())
                .build();
    }
}
