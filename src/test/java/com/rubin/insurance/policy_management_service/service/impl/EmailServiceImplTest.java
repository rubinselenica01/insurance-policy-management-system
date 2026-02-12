package com.rubin.insurance.policy_management_service.service.impl;

import com.rubin.insurance.policy_management_service.messaging.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.messaging.events.PolicyEventType;
import com.rubin.insurance.policy_management_service.messaging.events.payload.ClaimEventPayload;
import com.rubin.insurance.policy_management_service.messaging.events.payload.PolicyEventPayload;
import com.rubin.insurance.policy_management_service.model.entity.claim.ClaimStatus;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyStatus;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private SesClient sesClient;

    @InjectMocks
    private EmailServiceImpl emailService;

    private PolicyEventPayload policyPayload;
    private ClaimEventPayload claimPayload;

    @BeforeEach
    void setUp() {
        // Set the fromEmail property using ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@insurance.com");

        policyPayload = PolicyEventPayload.builder()
                .policyId(1L)
                .policyNumber("POL-2024-001")
                .customerEmail("customer@example.com")
                .status(PolicyStatus.ACTIVE)
                .policyType(PolicyType.HEALTH)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("500.00"))
                .build();

        claimPayload = new ClaimEventPayload(
                1L,
                "CLM-2024-001",
                1L,
                "customer@example.com",
                ClaimStatus.APPROVED,
                new BigDecimal("5000.00"),
                LocalDate.of(2024, 6, 15),
                null
        );
    }

    @Test
    void testSendPolicyCreatedEmail_Success() {
        // Arrange
        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // Act
        assertDoesNotThrow(() -> emailService.sendPolicyEventEmail(PolicyEventType.POLICY_CREATED, policyPayload));

        // Assert
        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(requestCaptor.capture());

        SendEmailRequest capturedRequest = requestCaptor.getValue();
        assertEquals("noreply@insurance.com", capturedRequest.source());
        assertTrue(capturedRequest.destination().toAddresses().contains("rubin.selenica@fshnstudent.info"));
        assertTrue(capturedRequest.message().subject().data().contains("POL-2024-001"));
        assertTrue(capturedRequest.message().body().html().data().contains("POL-2024-001"));
        assertTrue(capturedRequest.message().body().html().data().contains("HEALTH"));
        assertTrue(capturedRequest.message().body().html().data().contains("100000.00"));
    }

    @Test
    void testSendPolicyRenewedEmail_NoEmailSent() {
        // Act
        emailService.sendPolicyEventEmail(PolicyEventType.POLICY_RENEWED, policyPayload);

        // Assert - No email should be sent for POLICY_RENEWED
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void testSendPolicyCancelledEmail_NoEmailSent() {
        // Act
        emailService.sendPolicyEventEmail(PolicyEventType.POLICY_CANCELLED, policyPayload);

        // Assert - No email should be sent for POLICY_CANCELLED
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void testSendClaimApprovedEmail_Success() {
        // Arrange
        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // Act
        assertDoesNotThrow(() -> emailService.sendClaimEventEmail(ClaimEventType.CLAIM_APPROVED, claimPayload));

        // Assert
        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(requestCaptor.capture());

        SendEmailRequest capturedRequest = requestCaptor.getValue();
        assertEquals("noreply@insurance.com", capturedRequest.source());
        assertTrue(capturedRequest.destination().toAddresses().contains("customer@example.com"));
        assertTrue(capturedRequest.message().subject().data().contains("Claim Approved"));
        assertTrue(capturedRequest.message().subject().data().contains("CLM-2024-001"));
        assertTrue(capturedRequest.message().body().html().data().contains("CLM-2024-001"));
    }

    @Test
    void testSendClaimRejectedEmail_Success() {
        // Arrange
        ClaimEventPayload rejectedPayload = new ClaimEventPayload(
                1L,
                "CLM-2024-002",
                1L,
                "customer@example.com",
                ClaimStatus.REJECTED,
                new BigDecimal("5000.00"),
                LocalDate.of(2024, 6, 15),
                "Insufficient documentation provided"
        );

        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // Act
        assertDoesNotThrow(() -> emailService.sendClaimEventEmail(ClaimEventType.CLAIM_REJECTED, rejectedPayload));

        // Assert
        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(requestCaptor.capture());

        SendEmailRequest capturedRequest = requestCaptor.getValue();
        assertEquals("noreply@insurance.com", capturedRequest.source());
        assertTrue(capturedRequest.destination().toAddresses().contains("customer@example.com"));
        assertTrue(capturedRequest.message().subject().data().contains("Claim Status Update"));
        assertTrue(capturedRequest.message().subject().data().contains("CLM-2024-002"));
        assertTrue(capturedRequest.message().body().html().data().contains("Insufficient documentation provided"));
    }

    @Test
    void testSendClaimSubmittedEmail_NoEmailSent() {
        // Act
        emailService.sendClaimEventEmail(ClaimEventType.CLAIM_SUBMITTED, claimPayload);

        // Assert - No email should be sent for CLAIM_SUBMITTED
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void testTemplatePlaceholderReplacement_PolicyEmail() {
        // Arrange
        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // Act
        emailService.sendPolicyEventEmail(PolicyEventType.POLICY_CREATED, policyPayload);

        // Assert
        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(requestCaptor.capture());

        String emailBody = requestCaptor.getValue().message().body().html().data();
        
        // Verify all placeholders are replaced
        assertTrue(emailBody.contains("POL-2024-001"));
        assertTrue(emailBody.contains("HEALTH"));
        assertTrue(emailBody.contains("100000.00"));
        assertTrue(emailBody.contains("500.00"));
        assertTrue(emailBody.contains("2024-01-01"));
        assertTrue(emailBody.contains("2025-01-01"));
        assertTrue(emailBody.contains("ACTIVE"));
        
        // Verify no placeholders remain
        assertFalse(emailBody.contains("{{policyNumber}}"));
        assertFalse(emailBody.contains("{{policyType}}"));
        assertFalse(emailBody.contains("{{coverageAmount}}"));
        assertFalse(emailBody.contains("{{premiumAmount}}"));
    }

    @Test
    void testTemplatePlaceholderReplacement_ClaimEmail() {
        // Arrange
        SendEmailResponse response = SendEmailResponse.builder()
                .messageId("test-message-id")
                .build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        // Act
        emailService.sendClaimEventEmail(ClaimEventType.CLAIM_APPROVED, claimPayload);

        // Assert
        ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(requestCaptor.capture());

        String emailBody = requestCaptor.getValue().message().body().html().data();
        
        // Verify all placeholders are replaced
        assertTrue(emailBody.contains("CLM-2024-001"));
        assertTrue(emailBody.contains("5000.00"));
        assertTrue(emailBody.contains("2024-06-15"));
        assertTrue(emailBody.contains("APPROVED"));
        
        // Verify no placeholders remain
        assertFalse(emailBody.contains("{{claimNumber}}"));
        assertFalse(emailBody.contains("{{claimAmount}}"));
        assertFalse(emailBody.contains("{{incidentDate}}"));
    }
}
