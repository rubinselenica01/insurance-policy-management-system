package com.rubin.insurance.policy_management_service.controller;

import com.rubin.insurance.policy_management_service.configuration.exception_handling.ApiError;
import com.rubin.insurance.policy_management_service.model.dto.ApiResponseDTO;
import com.rubin.insurance.policy_management_service.model.dto.ClaimRequest;
import com.rubin.insurance.policy_management_service.model.dto.ClaimResponse;
import com.rubin.insurance.policy_management_service.model.dto.UpdateClaimStatusDTO;
import com.rubin.insurance.policy_management_service.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/claim")
@Slf4j
@Tag(name = "Claim", description = "APIs for submitting and managing insurance claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/create-new")
    @Operation(summary = "Create a new claim", description = "Submits a new claim for an existing policy. The claim starts in SUBMITTED status. Incident date must be today or in the past.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Claim created successfully\",\"data\":{\"id\":1,\"policyId\":1,\"claimNumber\":\"CLM-2025-00001\",\"description\":\"Vehicle damage from collision\",\"claimAmount\":5000.00,\"incidentDate\":\"2025-01-15\",\"status\":\"SUBMITTED\",\"rejectionReason\":null,\"createdAt\":\"2025-02-09T10:30:00Z\"}}}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g. malformed JSON) or business rule. When business rule: details.fieldErrors contains field name and error message(s).",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Invalid request body\",\"path\":\"/claim/create-new\",\"traceId\":null,\"details\":{\"fieldErrors\":{\"policyId\":[\"Policy not found\"],\"claimAmount\":[\"Claim amount exceeds policy coverage\"]}}}"))),
            @ApiResponse(responseCode = "404", description = "Policy not found for the given policyId",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Policy not found with id: 999\",\"path\":\"/claim/create-new\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "409", description = "Validation failed for business rule.",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Claims can be submitted only for ACTIVE policies\",\"path\":\"/claim/create-new\",\"traceId\":null,\"details\":{\"fieldErrors\":{}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/claim/create-new\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<ClaimResponse>> createClaim(@Valid @RequestBody ClaimRequest claimRequest) {
        ClaimResponse result = claimService.createClaim(claimRequest);
        return ResponseEntity.ok(ApiResponseDTO.success("Claim created successfully", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID", description = "Returns a single claim by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim found and returned",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Claim found successfully\",\"data\":{\"id\":1,\"policyId\":1,\"claimNumber\":\"CLM-2025-00001\",\"description\":\"Vehicle damage from collision\",\"claimAmount\":5000.00,\"incidentDate\":\"2025-01-15\",\"status\":\"SUBMITTED\",\"rejectionReason\":null,\"createdAt\":\"2025-02-09T10:30:00Z\"}}}"))),
            @ApiResponse(responseCode = "404", description = "Claim not found for the given ID",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Claim not found with id: 999\",\"path\":\"/claim/999\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/claim/1\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<ClaimResponse>> getClaimById(
            @Parameter(description = "Claim ID", example = "1", required = true) @PathVariable Long id) {
        ClaimResponse result = claimService.getClaimById(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Claim found successfully", result));
    }

    @GetMapping("/policy/{policyId}")
    @Operation(summary = "List claims by policy ID", description = "Returns all claims associated with the given policy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of claims for the policy (may be empty)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Claims found successfully\",\"data\":[{\"id\":1,\"policyId\":1,\"claimNumber\":\"CLM-2025-00001\",\"description\":\"Vehicle damage from collision\",\"claimAmount\":5000.00,\"incidentDate\":\"2025-01-15\",\"status\":\"SUBMITTED\",\"rejectionReason\":null,\"createdAt\":\"2025-02-09T10:30:00Z\"}]}"))),
            @ApiResponse(responseCode = "404", description = "Policy with specific id not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Policy not found with id: 999\",\"path\":\"/claim/policy/999\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/claim/policy/1\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<List<ClaimResponse>>> getClaimsByPolicyId(
            @Parameter(description = "Policy ID", example = "1", required = true) @PathVariable Long policyId) {
        List<ClaimResponse> resultList = claimService.getClaimsByPolicyId(policyId);
        return ResponseEntity.ok(ApiResponseDTO.success("Claims found successfully", resultList));
    }

    @PatchMapping("/{id}/update-status")
    @Operation(summary = "Update claim status", description = "Updates the status of a claim (SUBMITTED, APPROVED, REJECTED). When setting REJECTED, provide rejectDescription.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claim status updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Claim status updated successfully\",\"data\":{\"id\":1,\"policyId\":1,\"claimNumber\":\"CLM-2025-00001\",\"status\":\"APPROVED\",\"rejectionReason\":null}}"))),
            @ApiResponse(responseCode = "400", description = "Invalid status or validation error. details.fieldErrors: field name and error message(s).",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Invalid claim status\",\"path\":\"/claim/1/update-status\",\"traceId\":null,\"details\":{\"fieldErrors\":{\"claimStatus\":[\"must not be null\"],\"rejectDescription\":[\"required when status is REJECTED\"]}}}"))),
            @ApiResponse(responseCode = "404", description = "Claim not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Claim not found with id: 999\",\"path\":\"/claim/999/update-status\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "409", description = "Conflict with business requirements.",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Claim is already in final state and cannot be updated\",\"path\":\"/claim/1/update-status\",\"traceId\":null,\"details\":{\"fieldErrors\":{}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/claim/1/update-status\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<ClaimResponse>> updateClaimStatus(
            @Parameter(description = "Claim ID to update", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateClaimStatusDTO updateClaimStatusDTO) {
        ClaimResponse result = claimService.updateStatus(id, updateClaimStatusDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Claim status updated successfully", result));
    }
}
