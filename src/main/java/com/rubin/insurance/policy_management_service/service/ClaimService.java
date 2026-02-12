package com.rubin.insurance.policy_management_service.service;

import com.rubin.insurance.policy_management_service.model.dto.ClaimRequest;
import com.rubin.insurance.policy_management_service.model.dto.ClaimResponse;
import com.rubin.insurance.policy_management_service.model.dto.UpdateClaimStatusDTO;

import java.util.List;

public interface ClaimService {

    ClaimResponse createClaim(ClaimRequest claimRequest);

    ClaimResponse getClaimById(Long id);

    List<ClaimResponse> getClaimsByPolicyId(Long policyId);

    ClaimResponse updateStatus(Long id, UpdateClaimStatusDTO updateClaimStatusDTO);
}
