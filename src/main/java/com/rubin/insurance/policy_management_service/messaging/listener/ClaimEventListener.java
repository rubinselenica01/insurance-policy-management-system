package com.rubin.insurance.policy_management_service.messaging.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.rubin.insurance.policy_management_service.messaging.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.messaging.events.EventEnvelope;
import com.rubin.insurance.policy_management_service.messaging.events.payload.ClaimEventPayload;
import com.rubin.insurance.policy_management_service.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClaimEventListener {

    private final EmailService emailService;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "claim.events",
            groupId = "claim-processor",
            containerFactory = "kafkaListenerContainerFactory")
    public void onClaimEvent(EventEnvelope<?> envelope, Acknowledgment ack) {
        try {
            ClaimEventType eventType = ClaimEventType.valueOf(envelope.getEventType());
            log.info("Received claim event {} for entity {} at {}", eventType, envelope.getEntityId(), envelope.getOccurredAt());

            ClaimEventPayload payload = jsonMapper.convertValue(envelope.getPayload(), ClaimEventPayload.class);
            emailService.sendClaimEventEmail(eventType, payload);

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Error processing claim event {}", envelope, ex);
            throw ex; // DLQ handles
        }
    }
}
