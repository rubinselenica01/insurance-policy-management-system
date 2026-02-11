package com.rubin.insurance.policy_management_service.service.impl;

import com.rubin.insurance.policy_management_service.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.events.PolicyEventType;
import com.rubin.insurance.policy_management_service.events.payload.ClaimEventPayload;
import com.rubin.insurance.policy_management_service.events.payload.PolicyEventPayload;
import com.rubin.insurance.policy_management_service.model.claim.ClaimStatus;
import com.rubin.insurance.policy_management_service.model.policy.PolicyStatus;
import com.rubin.insurance.policy_management_service.model.policy.PolicyType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        assertThat(capturedRequest.source()).isEqualTo("noreply@insurance.com");
        assertThat(capturedRequest.destination().toAddresses()).contains("rubin.selenica@fshnstudent.info");
        assertThat(capturedRequest.message().subject().data()).contains("POL-2024-001");
        assertThat(capturedRequest.message().body().html().data()).contains("POL-2024-001");
        assertThat(capturedRequest.message().body().html().data()).contains("HEALTH");
        assertThat(capturedRequest.message().body().html().data()).contains("100000.00");
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
        assertThat(capturedRequest.source()).isEqualTo("noreply@insurance.com");
        assertThat(capturedRequest.destination().toAddresses()).contains("customer@example.com");
        assertThat(capturedRequest.message().subject().data()).contains("Claim Approved");
        assertThat(capturedRequest.message().subject().data()).contains("CLM-2024-001");
        assertThat(capturedRequest.message().body().html().data()).contains("CLM-2024-001");
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
        assertThat(capturedRequest.source()).isEqualTo("noreply@insurance.com");
        assertThat(capturedRequest.destination().toAddresses()).contains("customer@example.com");
        assertThat(capturedRequest.message().subject().data()).contains("Claim Status Update");
        assertThat(capturedRequest.message().subject().data()).contains("CLM-2024-002");
        assertThat(capturedRequest.message().body().html().data()).contains("Insufficient documentation provided");
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
        assertThat(emailBody).contains("POL-2024-001");
        assertThat(emailBody).contains("HEALTH");
        assertThat(emailBody).contains("100000.00");
        assertThat(emailBody).contains("500.00");
        assertThat(emailBody).contains("2024-01-01");
        assertThat(emailBody).contains("2025-01-01");
        assertThat(emailBody).contains("ACTIVE");
        
        // Verify no placeholders remain
        assertThat(emailBody).doesNotContain("{{policyNumber}}");
        assertThat(emailBody).doesNotContain("{{policyType}}");
        assertThat(emailBody).doesNotContain("{{coverageAmount}}");
        assertThat(emailBody).doesNotContain("{{premiumAmount}}");
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
        assertThat(emailBody).contains("CLM-2024-001");
        assertThat(emailBody).contains("5000.00");
        assertThat(emailBody).contains("2024-06-15");
        assertThat(emailBody).contains("APPROVED");
        
        // Verify no placeholders remain
        assertThat(emailBody).doesNotContain("{{claimNumber}}");
        assertThat(emailBody).doesNotContain("{{claimAmount}}");
        assertThat(emailBody).doesNotContain("{{incidentDate}}");
    }
}
