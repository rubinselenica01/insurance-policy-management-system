package com.rubin.insurance.policy_management_service.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.rubin.insurance.policy_management_service.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.events.PolicyEventType;
import com.rubin.insurance.policy_management_service.events.payload.ClaimEventPayload;
import com.rubin.insurance.policy_management_service.events.payload.PolicyEventPayload;
import com.rubin.insurance.policy_management_service.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final SesClient sesClient;

    @Value("${aws.ses.from-email}")
    private String fromEmail;

    private static final String POLICY_CREATED_TEMPLATE = "email-templates/policy-created.html";
    private static final String CLAIM_APPROVED_TEMPLATE = "email-templates/claim-approved.html";
    private static final String CLAIM_REJECTED_TEMPLATE = "email-templates/claim-rejected.html";

    @Override
    public void sendPolicyEventEmail(PolicyEventType eventType, PolicyEventPayload payload) {
        if (eventType != PolicyEventType.POLICY_CREATED) {
            log.debug("No email configured for policy event type: {}", eventType);
            return;
        }

        try {
            String htmlContent = loadTemplate(POLICY_CREATED_TEMPLATE);
            htmlContent = replacePolicyPlaceholders(htmlContent, payload);

            sendEmail(
                "rubin.selenica@fshnstudent.info",
                "Welcome to Your Insurance Policy - " + payload.policyNumber(),
                htmlContent
            );

            log.info("Policy created email sent successfully to {} for policy {}", 
                payload.customerEmail(), payload.policyNumber());
        } catch (Exception e) {
            log.error("Failed to send policy created email for policy {}: {}", 
                payload.policyNumber(), e.getMessage(), e);
            // Don't throw - handle gracefully as per requirements
        }
    }

    @Override
    public void sendClaimEventEmail(ClaimEventType eventType, ClaimEventPayload payload) {
        try {
            String template = switch (eventType) {
                case CLAIM_APPROVED -> CLAIM_APPROVED_TEMPLATE;
                case CLAIM_REJECTED -> CLAIM_REJECTED_TEMPLATE;
                case CLAIM_SUBMITTED -> {
                    log.debug("No email configured for claim event type: {}", eventType);
                    yield null;
                }
            };

            if (template == null) {
                return;
            }

            String htmlContent = loadTemplate(template);
            htmlContent = replaceClaimPlaceholders(htmlContent, payload);

            String subject = switch (eventType) {
                case CLAIM_APPROVED -> "Claim Approved - " + payload.getClaimNumber();
                case CLAIM_REJECTED -> "Claim Status Update - " + payload.getClaimNumber();
                default -> "Claim Update - " + payload.getClaimNumber();
            };

            sendEmail(payload.getCustomerEmail(), subject, htmlContent);

            log.info("{} email sent successfully to {} for claim {}", 
                eventType, payload.getCustomerEmail(), payload.getClaimNumber());
        } catch (Exception e) {
            log.error("Failed to send {} email for claim {}: {}", 
                eventType, payload.getClaimNumber(), e.getMessage(), e);
            // Don't throw - handle gracefully as per requirements
        }
    }

    private void sendEmail(String toEmail, String subject, String htmlBody) {
        try {
            Destination destination = Destination.builder()
                .toAddresses(toEmail)
                .build();

            Content subjectContent = Content.builder()
                .data(subject)
                .build();

            Content bodyContent = Content.builder()
                .data(htmlBody)
                .build();

            Body body = Body.builder()
                .html(bodyContent)
                .build();

            Message message = Message.builder()
                .subject(subjectContent)
                .body(body)
                .build();

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(destination)
                .message(message)
                .build();

            sesClient.sendEmail(emailRequest);
            log.debug("Email sent successfully via AWS SES to {}", toEmail);

        } catch (SesException e) {
            log.error("AWS SES error sending email to {}: {} - {}", 
                toEmail, e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to send email via SES", e);
        }
    }

    private String loadTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private String replacePolicyPlaceholders(String template, PolicyEventPayload payload) {
        return template
            .replace("{{policyNumber}}", payload.policyNumber())
            .replace("{{policyType}}", payload.policyType().toString())
            .replace("{{coverageAmount}}", payload.coverageAmount().toString())
            .replace("{{premiumAmount}}", payload.premiumAmount().toString())
            .replace("{{startDate}}", payload.startDate().toString())
            .replace("{{endDate}}", payload.endDate().toString())
            .replace("{{status}}", payload.status().toString());
    }

    private String replaceClaimPlaceholders(String template, ClaimEventPayload payload) {
        String result = template
            .replace("{{claimNumber}}", payload.getClaimNumber())
            .replace("{{policyId}}", payload.getPolicyId().toString())
            .replace("{{claimAmount}}", payload.getClaimAmount().toString())
            .replace("{{incidentDate}}", payload.getIncidentDate().toString())
            .replace("{{status}}", payload.getStatus().toString());

        // Handle optional rejection reason
        if (payload.getRejectionReason() != null && !payload.getRejectionReason().isEmpty()) {
            result = result.replace("{{rejectionReason}}", payload.getRejectionReason());
        } else {
            result = result.replace("{{rejectionReason}}", "Not specified");
        }

        return result;
    }
}
