package com.rubin.insurance.policy_management_service.configuration.exception_handling;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest req) {
        HttpStatus status = ex.status();
        ApiError body = buildError(status, ex.getMessage(), req, ex.details());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid"), Collectors.toList())
                ));

        ApiError body = buildError(HttpStatus.BAD_REQUEST, "Validation failed", req, Map.of("fieldErrors", fieldErrors));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error(ex.getMessage());
        ApiError body = buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, Map.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiError buildError(HttpStatus status, String message, HttpServletRequest req, Map<String, Object> details) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI(),
                null,
                details == null ? Map.of() : details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String message = "Malformed or unreadable JSON request body";
        if (ex.getRootCause() instanceof BadRequestException){
            message = ex.getRootCause().getMessage();
        }
        ApiError body = buildError(
                HttpStatus.BAD_REQUEST,
                message,
                req,
                Map.of()
        );

        return ResponseEntity.badRequest().body(body);
    }


}
