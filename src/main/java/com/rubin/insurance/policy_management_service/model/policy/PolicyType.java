package com.rubin.insurance.policy_management_service.model.policy;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.rubin.insurance.policy_management_service.configuration.exception_handling.BadRequestException;

import java.util.Arrays;

public enum PolicyType {
    HEALTH,
    AUTO,
    HOME,
    LIFE;

    @JsonCreator
    public static PolicyType fromValue(String v) {
        return Arrays.stream(PolicyType.values())
                .filter(el -> el.name().equalsIgnoreCase(v))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        String.format("Policy Type should be amongst : %s", Arrays.toString(PolicyType.values()))
                ));
    }
}
