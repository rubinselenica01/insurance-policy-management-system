package com.rubin.insurance.policy_management_service.service.impl;

import com.rubin.insurance.policy_management_service.configuration.exception_handling.NotFoundException;
import com.rubin.insurance.policy_management_service.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.dto.PolicyResponse;
import com.rubin.insurance.policy_management_service.events.PolicyEventType;
import com.rubin.insurance.policy_management_service.mapper.PolicyMapper;
import com.rubin.insurance.policy_management_service.messaging.PolicyEventPublisher;
import com.rubin.insurance.policy_management_service.model.policy.Policy;
import com.rubin.insurance.policy_management_service.repository.PolicyRepository;
import com.rubin.insurance.policy_management_service.service.PolicyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;
    private final PolicyEventPublisher policyEventPublisher;

    @Override
    public PolicyResponse savePolicy(PolicyRequest policy) {
        Policy mapped = policyMapper.toEntity(policy);
        Policy saved =  policyRepository.save(mapped);
        policyEventPublisher.publish(PolicyEventType.POLICY_CREATED, saved);
        return policyMapper.toDto(saved);
    }

    @Override
    public PolicyResponse getById(Long id) {
        Policy existing = reusableGetById(id);
        return policyMapper.toDto(existing);
    }

    @Override
    public Page<PolicyResponse> getAllPolicies(Pageable pageable) {
        Page<Policy> existingPage = policyRepository.findAll(pageable);
        return existingPage.map(policyMapper::toDto);
    }

    @Transactional
    @Override
    public PolicyResponse renewPolicy(Long id) {
        Policy existing = reusableGetById(id);
        existing.renew();
        Policy updated = policyRepository.save(existing);
        policyEventPublisher.publish(PolicyEventType.POLICY_RENEWED, updated);
        return policyMapper.toDto(updated);
    }

    @Transactional
    public void cancelPolicy(Long id) {
        Policy existing = reusableGetById(id);
        existing.cancel();
        Policy updated = policyRepository.save(existing);
        policyEventPublisher.publish(PolicyEventType.POLICY_CANCELLED, updated);
    }

    private Policy reusableGetById(Long id){
        return policyRepository.findById(id).orElseThrow(() -> new NotFoundException("Policy not found"));
    }

}
