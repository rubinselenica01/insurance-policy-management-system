package com.rubin.insurance.policy_management_service.repository;

import com.rubin.insurance.policy_management_service.model.claim.Claim;
import com.rubin.insurance.policy_management_service.model.policy.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim,Long> {

    List<Claim> findClaimsByPolicyId(Long policyId);
}
