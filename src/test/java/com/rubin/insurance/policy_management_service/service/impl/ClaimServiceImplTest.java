package com.rubin.insurance.policy_management_service.service.impl;

import com.rubin.insurance.policy_management_service.configuration.exception_handling.BusinessException;
import com.rubin.insurance.policy_management_service.configuration.exception_handling.NotFoundException;
import com.rubin.insurance.policy_management_service.model.dto.ClaimRequest;
import com.rubin.insurance.policy_management_service.model.dto.ClaimResponse;
import com.rubin.insurance.policy_management_service.model.dto.UpdateClaimStatusDTO;
import com.rubin.insurance.policy_management_service.messaging.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.model.mapper.ClaimMapper;
import com.rubin.insurance.policy_management_service.messaging.publisher.ClaimEventPublisher;
import com.rubin.insurance.policy_management_service.model.entity.claim.Claim;
import com.rubin.insurance.policy_management_service.model.entity.claim.ClaimStatus;
import com.rubin.insurance.policy_management_service.model.entity.policy.Policy;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyStatus;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyType;
import com.rubin.insurance.policy_management_service.repository.ClaimRepository;
import com.rubin.insurance.policy_management_service.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClaimMapper claimMapper;

    @Mock
    private ClaimEventPublisher claimEventPublisher;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private Policy activePolicy;
    private ClaimRequest claimRequest;
    private Claim claimEntity;
    private ClaimResponse claimResponse;

    @BeforeEach
    void setUp() {
        // Setup active policy
        activePolicy = Policy.builder()
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

        // Setup claim request
        claimRequest = new ClaimRequest(
                1L,
                "Medical treatment for injury",
                new BigDecimal("5000.00"),
                LocalDate.of(2024, 6, 15)
        );

        // Setup claim entity
        claimEntity = Claim.builder()
                .id(1L)
                .policy(activePolicy)
                .claimNumber("CLM-2024-000001")
                .description("Medical treatment for injury")
                .claimAmount(new BigDecimal("5000.00"))
                .incidentDate(LocalDate.of(2024, 6, 15))
                .status(ClaimStatus.SUBMITTED)
                .build();

        // Setup claim response
        claimResponse = new ClaimResponse(
                1L,
                1L,
                "CLM-2024-000001",
                "Medical treatment for injury",
                new BigDecimal("5000.00"),
                LocalDate.of(2024, 6, 15),
                "SUBMITTED",
                null,
                Instant.now()
        );
    }

    // ============ createClaim Tests ============

    @Test
    void testCreateClaim_Success() {
        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));
        when(claimMapper.toEntity(claimRequest)).thenReturn(claimEntity);
        when(claimRepository.save(claimEntity)).thenReturn(claimEntity);
        when(claimMapper.toDTO(claimEntity)).thenReturn(claimResponse);

        ClaimResponse result = claimService.createClaim(claimRequest);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("CLM-2024-000001", result.claimNumber());
        assertEquals("SUBMITTED", result.status());

        verify(policyRepository).findById(1L);
        verify(claimMapper).toEntity(claimRequest);
        verify(claimRepository).save(claimEntity);
        verify(claimMapper).toDTO(claimEntity);
        verify(claimEventPublisher).publish(ClaimEventType.CLAIM_SUBMITTED, claimEntity);
    }

    @Test
    void testCreateClaim_PolicyNotFound() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        ClaimRequest request = new ClaimRequest(
                999L,
                "Medical treatment",
                new BigDecimal("5000.00"),
                LocalDate.of(2024, 6, 15)
        );

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            claimService.createClaim(request);
        });
        assertEquals("Policy not found", exception.getMessage());

        verify(policyRepository).findById(999L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testCreateClaim_PolicyNotActive() {
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

        when(policyRepository.findById(1L)).thenReturn(Optional.of(cancelledPolicy));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            claimService.createClaim(claimRequest);
        });
        assertEquals("Claims can be submitted only for ACTIVE policies", exception.getMessage());

        verify(policyRepository).findById(1L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testCreateClaim_ClaimAmountExceedsCoverage() {
        ClaimRequest excessiveRequest = new ClaimRequest(
                1L,
                "Medical treatment",
                new BigDecimal("150000.00"), // Exceeds 100000 coverage
                LocalDate.of(2024, 6, 15)
        );

        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            claimService.createClaim(excessiveRequest);
        });
        assertEquals("Claim amount cannot exceed policy coverage amount!", exception.getMessage());

        verify(policyRepository).findById(1L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testCreateClaim_IncidentDateBeforePolicyStart() {
        ClaimRequest earlyRequest = new ClaimRequest(
                1L,
                "Medical treatment",
                new BigDecimal("5000.00"),
                LocalDate.of(2023, 12, 31) // Before policy starts on 2024-01-01
        );

        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            claimService.createClaim(earlyRequest);
        });
        assertEquals("Incident date should be between policy valid date!", exception.getMessage());

        verify(policyRepository).findById(1L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testCreateClaim_IncidentDateAfterPolicyEnd() {
        ClaimRequest lateRequest = new ClaimRequest(
                1L,
                "Medical treatment",
                new BigDecimal("5000.00"),
                LocalDate.of(2025, 1, 1) // After policy ends on 2024-12-31
        );

        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            claimService.createClaim(lateRequest);
        });
        assertEquals("Incident date should be between policy valid date!", exception.getMessage());

        verify(policyRepository).findById(1L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    // ============ getClaimById Tests ============

    @Test
    void testGetClaimById_Success() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claimEntity));
        when(claimMapper.toDTO(claimEntity)).thenReturn(claimResponse);

        ClaimResponse result = claimService.getClaimById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("CLM-2024-000001", result.claimNumber());

        verify(claimRepository).findById(1L);
        verify(claimMapper).toDTO(claimEntity);
    }

    @Test
    void testGetClaimById_NotFound() {
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            claimService.getClaimById(999L);
        });
        assertEquals("Claim not found", exception.getMessage());

        verify(claimRepository).findById(999L);
        verify(claimMapper, never()).toDTO(any());
    }

    // ============ getClaimsByPolicyId Tests ============

    @Test
    void testGetClaimsByPolicyId_WithMultipleClaims() {
        Claim claim2 = Claim.builder()
                .id(2L)
                .policy(activePolicy)
                .claimNumber("CLM-2024-000002")
                .description("Another claim")
                .claimAmount(new BigDecimal("3000.00"))
                .incidentDate(LocalDate.of(2024, 7, 20))
                .status(ClaimStatus.SUBMITTED)
                .build();

        ClaimResponse claimResponse2 = new ClaimResponse(
                2L,
                1L,
                "CLM-2024-000002",
                "Another claim",
                new BigDecimal("3000.00"),
                LocalDate.of(2024, 7, 20),
                "SUBMITTED",
                null,
                Instant.now()
        );

        List<Claim> claims = List.of(claimEntity, claim2);

        when(claimRepository.findClaimsByPolicyId(1L)).thenReturn(claims);
        when(claimMapper.toDTO(claimEntity)).thenReturn(claimResponse);
        when(claimMapper.toDTO(claim2)).thenReturn(claimResponse2);

        List<ClaimResponse> result = claimService.getClaimsByPolicyId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(claimRepository).findClaimsByPolicyId(1L);
        verify(claimMapper, times(2)).toDTO(any(Claim.class));
    }

    @Test
    void testGetClaimsByPolicyId_EmptyList_PolicyExists() {
        when(claimRepository.findClaimsByPolicyId(1L)).thenReturn(Collections.emptyList());
        when(policyRepository.existsById(1L)).thenReturn(true);

        List<ClaimResponse> result = claimService.getClaimsByPolicyId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(claimRepository).findClaimsByPolicyId(1L);
        verify(policyRepository).existsById(1L);
        verify(claimMapper, never()).toDTO(any());
    }

    @Test
    void testGetClaimsByPolicyId_PolicyNotFound() {
        when(claimRepository.findClaimsByPolicyId(999L)).thenReturn(Collections.emptyList());
        when(policyRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            claimService.getClaimsByPolicyId(999L);
        });
        assertEquals("Policy with id 999 not found", exception.getMessage());

        verify(claimRepository).findClaimsByPolicyId(999L);
        verify(policyRepository).existsById(999L);
    }

    // ============ updateStatus Tests ============

    @Test
    void testUpdateStatus_ToApproved_Success() {
        UpdateClaimStatusDTO updateDTO = new UpdateClaimStatusDTO(ClaimStatus.APPROVED, null);

        Claim approvedClaim = Claim.builder()
                .id(1L)
                .policy(activePolicy)
                .claimNumber("CLM-2024-000001")
                .description("Medical treatment for injury")
                .claimAmount(new BigDecimal("5000.00"))
                .incidentDate(LocalDate.of(2024, 6, 15))
                .status(ClaimStatus.APPROVED)
                .build();

        ClaimResponse approvedResponse = new ClaimResponse(
                1L,
                1L,
                "CLM-2024-000001",
                "Medical treatment for injury",
                new BigDecimal("5000.00"),
                LocalDate.of(2024, 6, 15),
                "APPROVED",
                null,
                Instant.now()
        );

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claimEntity));
        when(claimRepository.save(claimEntity)).thenReturn(approvedClaim);
        when(claimMapper.toDTO(approvedClaim)).thenReturn(approvedResponse);

        ClaimResponse result = claimService.updateStatus(1L, updateDTO);

        assertNotNull(result);
        assertEquals("APPROVED", result.status());

        verify(claimRepository).findById(1L);
        verify(claimRepository).save(claimEntity);
        verify(claimEventPublisher).publish(ClaimEventType.CLAIM_APPROVED, approvedClaim);
        verify(claimMapper).toDTO(approvedClaim);
    }

    @Test
    void testUpdateStatus_ToRejected_Success() {
        UpdateClaimStatusDTO updateDTO = new UpdateClaimStatusDTO(
                ClaimStatus.REJECTED,
                "Insufficient documentation"
        );

        Claim rejectedClaim = Claim.builder()
                .id(1L)
                .policy(activePolicy)
                .claimNumber("CLM-2024-000001")
                .description("Medical treatment for injury")
                .claimAmount(new BigDecimal("5000.00"))
                .incidentDate(LocalDate.of(2024, 6, 15))
                .status(ClaimStatus.REJECTED)
                .rejectionReason("Insufficient documentation")
                .build();

        ClaimResponse rejectedResponse = new ClaimResponse(
                1L,
                1L,
                "CLM-2024-000001",
                "Medical treatment for injury",
                new BigDecimal("5000.00"),
                LocalDate.of(2024, 6, 15),
                "REJECTED",
                "Insufficient documentation",
                Instant.now()
        );

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claimEntity));
        when(claimRepository.save(claimEntity)).thenReturn(rejectedClaim);
        when(claimMapper.toDTO(rejectedClaim)).thenReturn(rejectedResponse);

        ClaimResponse result = claimService.updateStatus(1L, updateDTO);

        assertNotNull(result);
        assertEquals("REJECTED", result.status());
        assertEquals("Insufficient documentation", result.rejectionReason());

        verify(claimRepository).findById(1L);
        verify(claimRepository).save(claimEntity);
        verify(claimEventPublisher).publish(ClaimEventType.CLAIM_REJECTED, rejectedClaim);
        verify(claimMapper).toDTO(rejectedClaim);
    }

    @Test
    void testUpdateStatus_ToSubmitted_Success() {
        UpdateClaimStatusDTO updateDTO = new UpdateClaimStatusDTO(ClaimStatus.SUBMITTED, null);

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claimEntity));
        when(claimRepository.save(claimEntity)).thenReturn(claimEntity);
        when(claimMapper.toDTO(claimEntity)).thenReturn(claimResponse);

        ClaimResponse result = claimService.updateStatus(1L, updateDTO);

        assertNotNull(result);
        assertEquals("SUBMITTED", result.status());

        verify(claimRepository).findById(1L);
        verify(claimRepository).save(claimEntity);
        verify(claimEventPublisher, never()).publish(any(), any()); // No event for SUBMITTED
        verify(claimMapper).toDTO(claimEntity);
    }

    @Test
    void testUpdateStatus_ClaimNotFound() {
        UpdateClaimStatusDTO updateDTO = new UpdateClaimStatusDTO(ClaimStatus.APPROVED, null);
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            claimService.updateStatus(999L, updateDTO);
        });
        assertEquals("Claim not found", exception.getMessage());

        verify(claimRepository).findById(999L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testUpdateStatus_RejectedWithoutDescription() {
        UpdateClaimStatusDTO updateDTO = new UpdateClaimStatusDTO(ClaimStatus.REJECTED, null);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claimEntity));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            claimService.updateStatus(1L, updateDTO);
        });
        assertEquals("Claim status cannot be updated without reject description", exception.getMessage());

        verify(claimRepository).findById(1L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testUpdateStatus_RejectedWithBlankDescription() {
        UpdateClaimStatusDTO updateDTO = new UpdateClaimStatusDTO(ClaimStatus.REJECTED, "   ");
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claimEntity));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            claimService.updateStatus(1L, updateDTO);
        });
        assertEquals("Claim status cannot be updated without reject description", exception.getMessage());

        verify(claimRepository).findById(1L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testUpdateStatus_CannotUpdateAlreadyApprovedClaim() {
        Claim approvedClaim = Claim.builder()
                .id(1L)
                .policy(activePolicy)
                .claimNumber("CLM-2024-000001")
                .description("Medical treatment for injury")
                .claimAmount(new BigDecimal("5000.00"))
                .incidentDate(LocalDate.of(2024, 6, 15))
                .status(ClaimStatus.APPROVED)
                .build();

        UpdateClaimStatusDTO updateDTO = new UpdateClaimStatusDTO(ClaimStatus.REJECTED, "Changed mind");
        when(claimRepository.findById(1L)).thenReturn(Optional.of(approvedClaim));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            claimService.updateStatus(1L, updateDTO);
        });
        assertEquals("Approved or rejected claims cannot change status.", exception.getMessage());

        verify(claimRepository).findById(1L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }

    @Test
    void testUpdateStatus_CannotUpdateAlreadyRejectedClaim() {
        Claim rejectedClaim = Claim.builder()
                .id(1L)
                .policy(activePolicy)
                .claimNumber("CLM-2024-000001")
                .description("Medical treatment for injury")
                .claimAmount(new BigDecimal("5000.00"))
                .incidentDate(LocalDate.of(2024, 6, 15))
                .status(ClaimStatus.REJECTED)
                .rejectionReason("Initial rejection")
                .build();

        UpdateClaimStatusDTO updateDTO = new UpdateClaimStatusDTO(ClaimStatus.APPROVED, null);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(rejectedClaim));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            claimService.updateStatus(1L, updateDTO);
        });
        assertEquals("Approved or rejected claims cannot change status.", exception.getMessage());

        verify(claimRepository).findById(1L);
        verify(claimRepository, never()).save(any());
        verify(claimEventPublisher, never()).publish(any(), any());
    }
}
