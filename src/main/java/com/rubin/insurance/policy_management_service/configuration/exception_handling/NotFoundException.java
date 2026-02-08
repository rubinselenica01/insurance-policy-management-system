package com.rubin.insurance.policy_management_service.configuration.exception_handling;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) { super(message); }

    @Override public HttpStatus status() { return HttpStatus.NOT_FOUND; }
}
