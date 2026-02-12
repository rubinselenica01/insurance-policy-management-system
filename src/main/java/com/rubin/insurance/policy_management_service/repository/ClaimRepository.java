package com.rubin.insurance.policy_management_service.repository;

import com.rubin.insurance.policy_management_service.model.entity.claim.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim,Long> {

    List<Claim> findClaimsByPolicyId(Long policyId);
}
