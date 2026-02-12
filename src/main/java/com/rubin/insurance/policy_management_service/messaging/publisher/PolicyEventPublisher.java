package com.rubin.insurance.policy_management_service.messaging.publisher;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.rubin.insurance.policy_management_service.messaging.events.EventEnvelope;
import com.rubin.insurance.policy_management_service.messaging.events.PolicyEventType;
import com.rubin.insurance.policy_management_service.messaging.events.payload.PolicyEventPayload;
import com.rubin.insurance.policy_management_service.model.entity.policy.Policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.rubin.insurance.policy_management_service.messaging.events.payload.PolicyEventPayload.buildPayload;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyEventPublisher {

    private static final String TOPIC = "policy.events";
    private static final String VERSION = "v1";

    private final KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate;

    @Value("${spring.application.name:policy-management-service}")
    private String appName;

    public void publish(PolicyEventType eventType, Policy policy) {
        EventEnvelope<PolicyEventPayload> envelope = EventEnvelope.<PolicyEventPayload>builder()
                .eventType(eventType.name())
                .entityId(String.valueOf(policy.getId()))
                .occurredAt(Instant.now())
                .traceId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .producerApp(appName)
                .version(VERSION)
                .payload(buildPayload(policy))
                .build();

        kafkaTemplate.send(TOPIC, envelope.getEntityId(), envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish policy event {} for policy {}", eventType, policy.getId(), ex);
                    } else {
                        log.info("Published policy event {} for policy {} to partition {} offset {}",
                                eventType, policy.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }


}
