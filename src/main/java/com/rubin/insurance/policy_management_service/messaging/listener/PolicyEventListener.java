package com.rubin.insurance.policy_management_service.messaging.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.rubin.insurance.policy_management_service.messaging.events.EventEnvelope;
import com.rubin.insurance.policy_management_service.messaging.events.PolicyEventType;
import com.rubin.insurance.policy_management_service.messaging.events.payload.PolicyEventPayload;
import com.rubin.insurance.policy_management_service.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class PolicyEventListener {

    private final EmailService emailService;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "policy.events",
            groupId = "policy-processor",
            containerFactory = "kafkaListenerContainerFactory")
    public void onPolicyEvent(EventEnvelope<PolicyEventPayload> envelope, Acknowledgment ack) {
        try {
            PolicyEventType eventType = PolicyEventType.valueOf(envelope.getEventType());
            log.info("Received policy event {} for entity {} at {}", eventType, envelope.getEntityId(), envelope.getOccurredAt());

            PolicyEventPayload payload = jsonMapper.convertValue(envelope.getPayload(), PolicyEventPayload.class);
            emailService.sendPolicyEventEmail(eventType, payload);

            ack.acknowledge();
        } catch (Exception ex) {
            log.warn("Policy event failed: {} - {}", envelope.getEventType(), ex.getMessage());
            throw ex; // DLQ handles
        }
    }
}