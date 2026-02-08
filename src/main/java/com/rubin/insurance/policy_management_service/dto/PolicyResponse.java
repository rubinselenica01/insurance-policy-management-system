package com.rubin.insurance.policy_management_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private String customerName;
    private String customerEmail;
    private String policyType;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private LocalDate startDate;
    private LocalDate endDate;
}
