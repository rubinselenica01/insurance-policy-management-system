package com.rubin.insurance.policy_management_service.messaging.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.rubin.insurance.policy_management_service.events.EventEnvelope;
import com.rubin.insurance.policy_management_service.events.PolicyEventType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PolicyEventListener {

    @KafkaListener(
            topics = "policy.events",
            groupId = "policy-processor",
            containerFactory = "kafkaListenerContainerFactory")
    public void onPolicyEvent(EventEnvelope<?> envelope, Acknowledgment ack) {
        try {
            PolicyEventType eventType = PolicyEventType.valueOf(envelope.getEventType());
            log.info("Received policy event {} for entity {} at {}", eventType, envelope.getEntityId(), envelope.getOccurredAt());

            // TODO:

            ack.acknowledge();
        } catch (Exception ex) {
            log.warn("Policy event failed: {} - {}", envelope.getEventType(), ex.getMessage());
            throw ex; // DLQ handles
        }
    }
}