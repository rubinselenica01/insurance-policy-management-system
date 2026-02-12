package com.rubin.insurance.policy_management_service.service;

import com.rubin.insurance.policy_management_service.messaging.events.ClaimEventType;
import com.rubin.insurance.policy_management_service.messaging.events.PolicyEventType;
import com.rubin.insurance.policy_management_service.messaging.events.payload.ClaimEventPayload;
import com.rubin.insurance.policy_management_service.messaging.events.payload.PolicyEventPayload;

public interface EmailService {
    void sendPolicyEventEmail(PolicyEventType eventType, PolicyEventPayload payload);
    void sendClaimEventEmail(ClaimEventType eventType, ClaimEventPayload payload);
}
