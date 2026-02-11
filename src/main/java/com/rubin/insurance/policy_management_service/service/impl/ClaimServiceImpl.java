package com.rubin.insurance.policy_management_service.service.impl;

import com.rubin.insurance.policy_management_service.configuration.exception_handling.BusinessException;
import com.rubin.insurance.policy_management_service.configuration.exception_handling.NotFoundException;
import com.rubin.insurance.policy_management_service.dto.ClaimRequest;
import com.rubin.insurance.policy_management_service.dto.ClaimResponse;
import com.rubin.insurance.policy_management_service.dto.UpdateClaimStatusDTO;
import com.rubin.insurance.policy_management_service.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.mapper.ClaimMapper;
import com.rubin.insurance.policy_management_service.messaging.ClaimEventPublisher;
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
    private final ClaimEventPublisher claimEventPublisher;

    @Override
    @Transactional
    public ClaimResponse createClaim(ClaimRequest claimRequest) {
        log.info("createClaim called for policyId={}", claimRequest.policyId());
        Policy existingPolicy = policyRepository.findById(claimRequest.policyId())
                .orElseThrow(() -> new NotFoundException("Policy not found"));
        validateClaim(existingPolicy, claimRequest);

        Claim claim = claimMapper.toEntity(claimRequest);
        claim.setPolicy(existingPolicy);

        Claim saved = claimRepository.save(claim);

        claimEventPublisher.publish(ClaimEventType.CLAIM_SUBMITTED, saved);

        return claimMapper.toDTO(saved);
    }

    @Override
    public ClaimResponse getClaimById(Long id) {
        log.info("getClaimById called with id={}", id);
        Claim existing =  reusableGetById(id);
        return claimMapper.toDTO(existing);
    }

    @Override
    public List<ClaimResponse> getClaimsByPolicyId(Long policyId) {
        log.info("getClaimsByPolicyId called with policyId={}", policyId);
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
        log.info("updateStatus called with id={} status={}", id, updateClaimStatusDTO.claimStatus());
        Claim existing = reusableGetById(id);

        if (updateClaimStatusDTO.claimStatus().equals(ClaimStatus.REJECTED)){
            if (updateClaimStatusDTO.rejectDescription() == null || updateClaimStatusDTO.rejectDescription().isBlank()){
                throw new BusinessException("Claim status cannot be updated without reject description");
            }
            existing.setRejectionReason(updateClaimStatusDTO.rejectDescription());
        }

        existing.setStatus(updateClaimStatusDTO.claimStatus());
        Claim saved = claimRepository.save(existing);

        if (saved.getStatus() == ClaimStatus.APPROVED) {
            claimEventPublisher.publish(ClaimEventType.CLAIM_APPROVED, saved);
        } else if (saved.getStatus() == ClaimStatus.REJECTED) {
            claimEventPublisher.publish(ClaimEventType.CLAIM_REJECTED, saved);
        }

        return claimMapper.toDTO(saved);
    }

    private Claim reusableGetById(Long id){
        log.debug("reusableGetById called with id={}", id);
        return claimRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Claim not found"));
    }
    private void validateClaim(Policy existingPolicy, ClaimRequest claimRequest){
        log.debug("validateClaim called for policyId={} claimAmount={}",
                existingPolicy.getId(), claimRequest.claimAmount());
        if(!existingPolicy.getStatus().equals(PolicyStatus.ACTIVE)){
            throw new BusinessException("Claims can be submitted only for ACTIVE policies");
        }else if(claimRequest.claimAmount().compareTo(existingPolicy.getCoverageAmount()) == 1){
            throw new BusinessException("Claim amount cannot exceed policy coverage amount!");
        }else if(claimRequest.incidentDate().isBefore(existingPolicy.getStartDate()) || claimRequest.incidentDate().isAfter(existingPolicy.getEndDate())){
            throw new BusinessException("Incident date should be between policy valid date!");
        }
    }
}
