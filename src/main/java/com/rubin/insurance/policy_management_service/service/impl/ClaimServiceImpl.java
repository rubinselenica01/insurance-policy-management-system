package com.rubin.insurance.policy_management_service.service.impl;

import com.rubin.insurance.policy_management_service.configuration.exception_handling.BusinessException;
import com.rubin.insurance.policy_management_service.configuration.exception_handling.NotFoundException;
import com.rubin.insurance.policy_management_service.dto.ClaimRequest;
import com.rubin.insurance.policy_management_service.dto.ClaimResponse;
import com.rubin.insurance.policy_management_service.dto.UpdateClaimStatusDTO;
import com.rubin.insurance.policy_management_service.mapper.ClaimMapper;
import com.rubin.insurance.policy_management_service.model.claim.Claim;
import com.rubin.insurance.policy_management_service.model.claim.ClaimStatus;
import com.rubin.insurance.policy_management_service.model.policy.Policy;
import com.rubin.insurance.policy_management_service.model.policy.PolicyStatus;
import com.rubin.insurance.policy_management_service.repository.ClaimRepository;
import com.rubin.insurance.policy_management_service.repository.PolicyRepository;
import com.rubin.insurance.policy_management_service.service.ClaimService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimMapper claimMapper;
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;

    @Override
    @Transactional
    public ClaimResponse createClaim(ClaimRequest claimRequest) {
        Policy existingPolicy = policyRepository.findById(claimRequest.getPolicyId())
                .orElseThrow(() -> new NotFoundException("Policy not found"));
        validateClaim(existingPolicy, claimRequest);

        Claim claim = claimMapper.toEntity(claimRequest);
        claim.setPolicy(existingPolicy);

        Claim saved = claimRepository.save(claim);

        return claimMapper.toDTO(saved);
    }

    @Override
    public ClaimResponse getClaimById(Long id) {
        Claim existing =  reusableGetById(id);
        return claimMapper.toDTO(existing);
    }

    @Override
    public List<ClaimResponse> getClaimsByPolicyId(Long policyId) {
        List<Claim> claimsList = claimRepository.findClaimsByPolicyId(policyId);
        if (claimsList.isEmpty() && !policyRepository.existsById(policyId)) {
            throw new NotFoundException(String.format("Policy with id %d not found", policyId));
        }
        return claimsList.stream()
                .map(claimMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClaimResponse updateStatus(Long id, UpdateClaimStatusDTO updateClaimStatusDTO) {
        Claim existing = reusableGetById(id);

        if (updateClaimStatusDTO.claimStatus().equals(ClaimStatus.REJECTED)){
            if (updateClaimStatusDTO.rejectDescription() == null || updateClaimStatusDTO.rejectDescription().isBlank()){
                throw new BusinessException("Claim status cannot be updated without reject description");
            }
            existing.setRejectionReason(updateClaimStatusDTO.rejectDescription());
        }

        existing.setStatus(updateClaimStatusDTO.claimStatus());
        Claim saved = claimRepository.save(existing);
        return claimMapper.toDTO(saved);
    }

    private Claim reusableGetById(Long id){
        return claimRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Claim not found"));
    }
    private void validateClaim(Policy existingPolicy, ClaimRequest claimRequest){
        if(!existingPolicy.getStatus().equals(PolicyStatus.ACTIVE)){
            throw new BusinessException("Claims can be submitted only for ACTIVE policies");
        }else if(claimRequest.getClaimAmount().compareTo(existingPolicy.getCoverageAmount()) == 1){
            throw new BusinessException("Claim amount cannot exceed policy coverage amount!");
        }else if(!(claimRequest.getIncidentDate().isAfter(existingPolicy.getStartDate()) && claimRequest.getIncidentDate().isBefore(existingPolicy.getEndDate()))){
            throw new BusinessException("Incident date should be between policy valid date!");
        }
    }
}
