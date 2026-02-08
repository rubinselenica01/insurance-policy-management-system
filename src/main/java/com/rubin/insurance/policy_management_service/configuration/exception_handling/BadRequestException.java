package com.rubin.insurance.policy_management_service.configuration.exception_handling;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) { super(message); }

    @Override public org.springframework.http.HttpStatus status() { return org.springframework.http.HttpStatus.BAD_REQUEST; }
}
