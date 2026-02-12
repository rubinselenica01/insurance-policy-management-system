package com.rubin.insurance.policy_management_service.service;

import com.rubin.insurance.policy_management_service.model.dto.PageResponse;
import com.rubin.insurance.policy_management_service.model.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.model.dto.PolicyResponse;
import org.springframework.data.domain.Pageable;

public interface PolicyService {

    PolicyResponse savePolicy(PolicyRequest policy);

    PolicyResponse getById(Long id);

    PageResponse<PolicyResponse> getAllPolicies(Pageable pageable);

    PolicyResponse renewPolicy(Long id);

    PolicyResponse cancelPolicy(Long id);
}
