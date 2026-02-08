package com.rubin.insurance.policy_management_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class ClaimResponse {
    private Long id;
    private Long policyId;
    private String claimNumber;
    private String description;
    private BigDecimal claimAmount;
    private LocalDate incidentDate;
    private String status;
    private String rejectionReason;
    private Instant createdAt;
}
