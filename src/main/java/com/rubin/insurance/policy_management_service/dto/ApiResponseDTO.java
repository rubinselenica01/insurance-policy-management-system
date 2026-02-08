package com.rubin.insurance.policy_management_service.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object for standardized API responses.
 *
 * This class provides a consistent structure for all API responses in the application,
 * with support for success and error responses, error codes, and a generic data payload.
 * It includes static factory methods to create common response types.
 *
 * @param <T> the type of data payload contained in the response
 *
 * @author SuperAnalyst Team
 * @since 1.0
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {
    /**
     * Status of the response, either "SUCCESS" or "ERROR".
     */
    private String status;

    /**
     * A human-readable message describing the response.
     */
    private String message;

    /**
     * Optional error code for error responses.
     */
    private Integer errorCode;

    /**
     * ISO formatted timestamp indicating when the response was created.
     */
    private String timestamp;

    /**
     * The payload of the response. This field is always included in the JSON,
     * even when null, to maintain a consistent response structure.
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private T data;

    public ApiResponseDTO(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public ApiResponseDTO(String status, String message, Integer errorCode, T data) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>("SUCCESS", message, data);
    }


    public static <T> ResponseEntity<ApiResponseDTO<PageResponse<T>>> success(Page<T> page, String message){
        PageResponse<T> pageResponse = new PageResponse<>(page);
        return ResponseEntity.ok(ApiResponseDTO.success(message, pageResponse));
    }


}