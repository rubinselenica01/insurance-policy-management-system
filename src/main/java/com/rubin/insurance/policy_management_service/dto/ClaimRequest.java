package com.rubin.insurance.policy_management_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClaimRequest {

    @NotNull(message = "Policy Id should be present")
    private Long policyId;

    @NotBlank(message = "Description should not be empty")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Claim amount should not be empty")
    @Positive(message = "Claim amount should be positive number")
    private BigDecimal claimAmount;

    @NotNull(message = "Incident Date cannot be empty!")
    @PastOrPresent(message = "Incident Date should not be in the future")
    private LocalDate incidentDate;
}
