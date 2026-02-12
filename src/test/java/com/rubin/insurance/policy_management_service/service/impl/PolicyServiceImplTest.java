package com.rubin.insurance.policy_management_service.service.impl;

import com.rubin.insurance.policy_management_service.configuration.exception_handling.BusinessException;
import com.rubin.insurance.policy_management_service.configuration.exception_handling.NotFoundException;
import com.rubin.insurance.policy_management_service.model.dto.PageResponse;
import com.rubin.insurance.policy_management_service.model.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.model.dto.PolicyResponse;
import com.rubin.insurance.policy_management_service.messaging.events.PolicyEventType;
import com.rubin.insurance.policy_management_service.model.mapper.PolicyMapper;
import com.rubin.insurance.policy_management_service.messaging.publisher.PolicyEventPublisher;
import com.rubin.insurance.policy_management_service.model.entity.policy.Policy;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyStatus;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyType;
import com.rubin.insurance.policy_management_service.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyMapper policyMapper;

    @Mock
    private PolicyEventPublisher policyEventPublisher;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private PolicyRequest policyRequest;
    private Policy policyEntity;
    private PolicyResponse policyResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        policyRequest = new PolicyRequest(
                "John Smith",
                "john.smith@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        policyEntity = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-000001")
                .customerName("John Smith")
                .customerEmail("john.smith@example.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .status(PolicyStatus.ACTIVE)
                .build();

        policyResponse = new PolicyResponse(
                1L,
                "POL-2024-000001",
                "John Smith",
                "john.smith@example.com",
                "HEALTH",
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "ACTIVE"
        );
    }

    // ============ savePolicy Tests ============

    @Test
    void testSavePolicy_Success() {
        when(policyMapper.toEntity(policyRequest)).thenReturn(policyEntity);
        when(policyRepository.save(policyEntity)).thenReturn(policyEntity);
        when(policyMapper.toDto(policyEntity)).thenReturn(policyResponse);

        PolicyResponse result = policyService.savePolicy(policyRequest);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("POL-2024-000001", result.policyNumber());
        assertEquals("John Smith", result.customerName());
        assertEquals("ACTIVE", result.policyStatus());

        verify(policyMapper).toEntity(policyRequest);
        verify(policyRepository).save(policyEntity);
        verify(policyMapper).toDto(policyEntity);
        verify(policyEventPublisher).publish(PolicyEventType.POLICY_CREATED, policyEntity);
    }

    // ============ getById Tests ============

    @Test
    void testGetById_Success() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policyEntity));
        when(policyMapper.toDto(policyEntity)).thenReturn(policyResponse);

        PolicyResponse result = policyService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("POL-2024-000001", result.policyNumber());

        verify(policyRepository).findById(1L);
        verify(policyMapper).toDto(policyEntity);
    }

    @Test
    void testGetById_NotFound() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            policyService.getById(999L);
        });
        assertEquals("Policy not found", exception.getMessage());

        verify(policyRepository).findById(999L);
        verify(policyMapper, never()).toDto(any());
    }

    // ============ getAllPolicies Tests ============

    @Test
    void testGetAllPolicies_WithData() {
        // Arrange
        Policy policy2 = Policy.builder()
                .id(2L)
                .policyNumber("POL-2024-000002")
                .customerName("Jane Doe")
                .customerEmail("jane.doe@example.com")
                .policyType(PolicyType.AUTO)
                .coverageAmount(new BigDecimal("50000.00"))
                .premiumAmount(new BigDecimal("300.00"))
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 8, 1))
                .status(PolicyStatus.ACTIVE)
                .build();

        PolicyResponse policyResponse2 = new PolicyResponse(
                2L,
                "POL-2024-000002",
                "Jane Doe",
                "jane.doe@example.com",
                "AUTO",
                new BigDecimal("50000.00"),
                new BigDecimal("300.00"),
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 8, 1),
                "ACTIVE"
        );

        List<Policy> policies = List.of(policyEntity, policy2);
        Page<Policy> policyPage = new PageImpl<>(policies, PageRequest.of(0, 10), 2);

        Pageable pageable = PageRequest.of(0, 10);
        when(policyRepository.findAll(pageable)).thenReturn(policyPage);
        when(policyMapper.toDto(policyEntity)).thenReturn(policyResponse);
        when(policyMapper.toDto(policy2)).thenReturn(policyResponse2);

        PageResponse<PolicyResponse> result = policyService.getAllPolicies(pageable);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(2, result.pagination().totalElements());
        assertEquals(1, result.pagination().totalPages());
        assertEquals(1, result.pagination().page()); // 1-based page number
        assertEquals(10, result.pagination().size());

        verify(policyRepository).findAll(pageable);
        verify(policyMapper, times(2)).toDto(any(Policy.class));
    }

    @Test
    void testGetAllPolicies_EmptyPage() {
        Page<Policy> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        Pageable pageable = PageRequest.of(0, 10);
        when(policyRepository.findAll(pageable)).thenReturn(emptyPage);

        PageResponse<PolicyResponse> result = policyService.getAllPolicies(pageable);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagination().totalElements());
        assertEquals(0, result.pagination().totalPages());
        assertTrue(result.pagination().empty());

        verify(policyRepository).findAll(pageable);
        verify(policyMapper, never()).toDto(any());
    }

    // ============ renewPolicy Tests ============

    @Test
    void testRenewPolicy_Success_ExpiredPolicy() {
        Policy expiredPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-000001")
                .customerName("John Smith")
                .customerEmail("john.smith@example.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.of(2023, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .status(PolicyStatus.ACTIVE)
                .build();

        Policy renewedPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-000001")
                .customerName("John Smith")
                .customerEmail("john.smith@example.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .status(PolicyStatus.ACTIVE)
                .build();

        PolicyResponse renewedResponse = new PolicyResponse(
                1L,
                "POL-2024-000001",
                "John Smith",
                "john.smith@example.com",
                "HEALTH",
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                "ACTIVE"
        );

        when(policyRepository.findById(1L)).thenReturn(Optional.of(expiredPolicy));
        when(policyRepository.save(expiredPolicy)).thenReturn(renewedPolicy);
        when(policyMapper.toDto(renewedPolicy)).thenReturn(renewedResponse);

        PolicyResponse result = policyService.renewPolicy(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(policyRepository).findById(1L);
        verify(policyRepository).save(expiredPolicy);
        verify(policyEventPublisher).publish(PolicyEventType.POLICY_RENEWED, renewedPolicy);
        verify(policyMapper).toDto(renewedPolicy);
    }

    @Test
    void testRenewPolicy_Success_InactivePolicy() {
        Policy cancelledPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-000001")
                .customerName("John Smith")
                .customerEmail("john.smith@example.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.of(2023, 1, 1))
                .endDate(LocalDate.now().minusDays(1))
                .status(PolicyStatus.CANCELLED)
                .build();

        when(policyRepository.findById(1L)).thenReturn(Optional.of(cancelledPolicy));
        when(policyRepository.save(cancelledPolicy)).thenReturn(cancelledPolicy);
        when(policyMapper.toDto(any(Policy.class))).thenReturn(policyResponse);

        PolicyResponse result = policyService.renewPolicy(1L);

        assertNotNull(result);
        verify(policyRepository).findById(1L);
        verify(policyRepository).save(cancelledPolicy);
        verify(policyEventPublisher).publish(eq(PolicyEventType.POLICY_RENEWED), any(Policy.class));
    }

    @Test
    void testRenewPolicy_PolicyNotFound() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            policyService.renewPolicy(999L);
        });
        assertEquals("Policy not found", exception.getMessage());

        verify(policyRepository).findById(999L);
        verify(policyRepository, never()).save(any());
        verify(policyEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testRenewPolicy_CannotRenewActivePolicy() {
        // Arrange - Active policy with future end date
        Policy activePolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-000001")
                .customerName("John Smith")
                .customerEmail("john.smith@example.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(5))
                .status(PolicyStatus.ACTIVE)
                .build();

        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            policyService.renewPolicy(1L);
        });
        assertEquals("You can't renew until the first period finishes", exception.getMessage());

        verify(policyRepository).findById(1L);
        verify(policyRepository, never()).save(any());
        verify(policyEventPublisher, never()).publish(any(), any());
    }

    // ============ cancelPolicy Tests ============

    @Test
    void testCancelPolicy_Success() {
        // Arrange
        Policy activePolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-000001")
                .customerName("John Smith")
                .customerEmail("john.smith@example.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .status(PolicyStatus.ACTIVE)
                .build();

        Policy cancelledPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-000001")
                .customerName("John Smith")
                .customerEmail("john.smith@example.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .status(PolicyStatus.CANCELLED)
                .build();

        PolicyResponse cancelledResponse = new PolicyResponse(
                1L,
                "POL-2024-000001",
                "John Smith",
                "john.smith@example.com",
                "HEALTH",
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "CANCELLED"
        );

        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));
        when(policyRepository.save(activePolicy)).thenReturn(cancelledPolicy);
        when(policyMapper.toDto(cancelledPolicy)).thenReturn(cancelledResponse);

        PolicyResponse result = policyService.cancelPolicy(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("CANCELLED", result.policyStatus());

        verify(policyRepository).findById(1L);
        verify(policyRepository).save(activePolicy);
        verify(policyEventPublisher).publish(PolicyEventType.POLICY_CANCELLED, cancelledPolicy);
        verify(policyMapper).toDto(cancelledPolicy);
    }

    @Test
    void testCancelPolicy_PolicyNotFound() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            policyService.cancelPolicy(999L);
        });
        assertEquals("Policy not found", exception.getMessage());

        verify(policyRepository).findById(999L);
        verify(policyRepository, never()).save(any());
        verify(policyEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testCancelPolicy_CannotCancelNonActivePolicy() {
        Policy alreadyCancelledPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-000001")
                .customerName("John Smith")
                .customerEmail("john.smith@example.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .status(PolicyStatus.CANCELLED)
                .build();

        when(policyRepository.findById(1L)).thenReturn(Optional.of(alreadyCancelledPolicy));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            policyService.cancelPolicy(1L);
        });
        assertEquals("Only ACTIVE policies can be cancelled.", exception.getMessage());

        verify(policyRepository).findById(1L);
        verify(policyRepository, never()).save(any());
        verify(policyEventPublisher, never()).publish(any(), any());
    }
}
