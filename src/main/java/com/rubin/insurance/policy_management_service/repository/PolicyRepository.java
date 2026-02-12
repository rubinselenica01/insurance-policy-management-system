package com.rubin.insurance.policy_management_service.repository;

import com.rubin.insurance.policy_management_service.model.entity.policy.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy,Long> {

    Page<Policy> findAll(Pageable pageable);
}
