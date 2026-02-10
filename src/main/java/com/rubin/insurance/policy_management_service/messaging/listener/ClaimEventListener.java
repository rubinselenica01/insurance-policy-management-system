package com.rubin.insurance.policy_management_service.messaging.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.rubin.insurance.policy_management_service.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.events.EventEnvelope;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ClaimEventListener {

    @KafkaListener(
            topics = "claim.events",
            groupId = "claim-processor",
            containerFactory = "kafkaListenerContainerFactory")
    public void onClaimEvent(EventEnvelope<?> envelope, Acknowledgment ack) {
        try {
            ClaimEventType eventType = ClaimEventType.valueOf(envelope.getEventType());
            log.info("Received claim event {} for entity {} at {}", eventType, envelope.getEntityId(), envelope.getOccurredAt());

            // TODO:

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Error processing claim event {}", envelope, ex);
            throw ex; // DLQ handles
        }
    }
}
