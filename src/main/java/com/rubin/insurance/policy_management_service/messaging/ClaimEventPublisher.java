package com.rubin.insurance.policy_management_service.messaging;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.rubin.insurance.policy_management_service.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.events.EventEnvelope;
import com.rubin.insurance.policy_management_service.events.payload.ClaimEventPayload;
import com.rubin.insurance.policy_management_service.model.claim.Claim;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.rubin.insurance.policy_management_service.events.payload.ClaimEventPayload.buildPayload;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimEventPublisher {

    private static final String TOPIC = "claim.events";
    private static final String VERSION = "v1";

    private final KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate;

    @Value("${spring.application.name:policy-management-service}")
    private String appName;

    public void publish(ClaimEventType eventType, Claim claim) {
        EventEnvelope<ClaimEventPayload> envelope = EventEnvelope.<ClaimEventPayload>builder()
                .eventType(eventType.name())
                .entityId(String.valueOf(claim.getId()))
                .occurredAt(Instant.now())
                .traceId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .producerApp(appName)
                .version(VERSION)
                .payload(buildPayload(claim))
                .build();

        kafkaTemplate.send(TOPIC, envelope.getEntityId(), envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish claim event {} for claim {}", eventType, claim.getId(), ex);
                    } else {
                        log.info("Published claim event {} for claim {} to partition {} offset {}",
                                eventType, claim.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

}
