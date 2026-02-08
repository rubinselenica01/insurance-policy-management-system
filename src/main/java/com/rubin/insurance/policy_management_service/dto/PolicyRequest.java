package com.rubin.insurance.policy_management_service.dto;

import com.rubin.insurance.policy_management_service.dto.validators.CustomerFullName;

import com.rubin.insurance.policy_management_service.dto.validators.Email;
import com.rubin.insurance.policy_management_service.dto.validators.PremiumAmount;
import com.rubin.insurance.policy_management_service.dto.validators.ValidDateRange;
import com.rubin.insurance.policy_management_service.model.policy.PolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@PremiumAmount(coverageAmount = "coverageAmount", premiumAmount = "premiumAmount")
@ValidDateRange
public class PolicyRequest {

    @NotBlank(message = "Customer name should not be empty")
    @CustomerFullName
    private String customerName;

    @NotBlank(message = "Customer email should not be empty")
    @Email
    private String customerEmail;

    @NotNull(message = "Policy type should not be empty")
    private PolicyType policyType;

    @NotNull(message = "Coverage Amount should not be empty")
    @Positive(message = "Coverage Amount should be a positive value")
    private BigDecimal coverageAmount;

    @NotNull(message = "Premium Amount should not be empty")
    @Positive(message = "Premium Amount should be a positive value")
    private BigDecimal premiumAmount;

    @NotNull(message = "Start Date should not be empty")
    private LocalDate startDate;

    @NotNull(message = "End Date should not be empty")
    private LocalDate endDate;
}
