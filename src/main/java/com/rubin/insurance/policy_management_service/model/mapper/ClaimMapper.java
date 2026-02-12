package com.rubin.insurance.policy_management_service.model.mapper;

import com.rubin.insurance.policy_management_service.model.dto.ClaimRequest;
import com.rubin.insurance.policy_management_service.model.dto.ClaimResponse;
import com.rubin.insurance.policy_management_service.model.entity.claim.Claim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    Claim toEntity(ClaimRequest claimRequest);

    @Mapping(source = "policy.id", target = "policyId")
    ClaimResponse toDTO(Claim claim);

}
