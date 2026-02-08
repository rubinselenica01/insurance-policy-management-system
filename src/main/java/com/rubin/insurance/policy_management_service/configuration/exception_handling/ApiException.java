package com.rubin.insurance.policy_management_service.configuration.exception_handling;

import java.util.Map;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);

    }
    public abstract org.springframework.http.HttpStatus status();

    public Map<String, Object> details() { return Map.of(); }
}
