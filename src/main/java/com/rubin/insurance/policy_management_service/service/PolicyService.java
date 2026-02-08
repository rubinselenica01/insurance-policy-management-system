package com.rubin.insurance.policy_management_service.service;

import com.rubin.insurance.policy_management_service.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.dto.PolicyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyService {

    PolicyResponse savePolicy(PolicyRequest policy);

    PolicyResponse getById(Long id);

    Page<PolicyResponse> getAllPolicies(Pageable pageable);

    PolicyResponse renewPolicy(Long id);

    void cancelPolicy(Long id);
}
