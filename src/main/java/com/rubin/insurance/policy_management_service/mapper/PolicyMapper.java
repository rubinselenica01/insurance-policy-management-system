package com.rubin.insurance.policy_management_service.mapper;

import com.rubin.insurance.policy_management_service.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.dto.PolicyResponse;
import com.rubin.insurance.policy_management_service.model.policy.Policy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

    Policy toEntity(PolicyRequest policyRequest);

    PolicyResponse toDto(Policy policy);
}
