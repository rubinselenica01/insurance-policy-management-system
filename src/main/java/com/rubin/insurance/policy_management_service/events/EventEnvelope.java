package com.rubin.insurance.policy_management_service.events;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {
    private String eventType;
    private String entityId;
    private Instant occurredAt;
    private String traceId;
    private String correlationId;
    private String producerApp;
    private String version;
    private T payload;
}
