package com.rubin.insurance.policy_management_service.controller;


import com.rubin.insurance.policy_management_service.configuration.exception_handling.ApiError;
import com.rubin.insurance.policy_management_service.model.dto.ApiResponseDTO;
import com.rubin.insurance.policy_management_service.model.dto.PageResponse;
import com.rubin.insurance.policy_management_service.model.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.model.dto.PolicyResponse;
import com.rubin.insurance.policy_management_service.service.PolicyService;
import com.rubin.insurance.policy_management_service.utils.PaginationUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/policy")
@Slf4j
@Tag(name = "Policy", description = "APIs for creating and managing insurance policies (health, auto, home, life)")
public class PolicyController {

    private final PolicyService policyService;

    @Autowired
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/create-new")
    @Operation(summary = "Create a new policy", description = "Creates a new insurance policy with the provided customer and coverage details. Policy type must be one of: HEALTH, AUTO, HOME, LIFE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Policy created successfully\",\"data\":{\"id\":1,\"policyNumber\":\"POL-2025-00001\",\"customerName\":\"John Smith\",\"customerEmail\":\"john.smith@example.com\",\"policyType\":\"HEALTH\",\"coverageAmount\":100000.00,\"premiumAmount\":150.50,\"startDate\":\"2025-01-01\",\"endDate\":\"2026-01-01\"}}}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g. malformed JSON or business rule). When business rule: details contains field name(s) and error message(s).",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"End date must be after start date\",\"path\":\"/policy/create-new\",\"traceId\":null,\"details\":{\"fieldErrors\":{\"endDate\":[\"End date must be after start date\"],\"premiumAmount\":[\"Premium amount must be less than coverage amount\"]}}}"))),
//            @ApiResponse(responseCode = "409", description = "Validation failed (e.g. @Valid). details.fieldErrors: map of field name to list of error messages.",
//                    content = @Content(schema = @Schema(implementation = ApiError.class),
//                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Validation failed\",\"path\":\"/policy/create-new\",\"traceId\":null,\"details\":{\"fieldErrors\":{\"customerEmail\":[\"Invalid email format\"],\"customerName\":[\"Must be first and last name\"],\"startDate\":[\"End date must be after start date\"]}}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/policy/create-new\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<PolicyResponse>> createPolicy(@Valid @RequestBody PolicyRequest policyRequest) {
        PolicyResponse response = policyService.savePolicy(policyRequest);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Policy created successfully", response)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID", description = "Returns a single policy by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy found and returned",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Policy retrieved successfully\",\"data\":{\"id\":1,\"policyNumber\":\"POL-2025-00001\",\"customerName\":\"John Smith\",\"customerEmail\":\"john.smith@example.com\",\"policyType\":\"HEALTH\",\"coverageAmount\":100000.00,\"premiumAmount\":150.50,\"startDate\":\"2025-01-01\",\"endDate\":\"2026-01-01\"}}}"))),
            @ApiResponse(responseCode = "404", description = "Policy not found for the given ID",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Policy not found with id: 999\",\"path\":\"/policy/999\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/policy/1\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<PolicyResponse>> getPolicy(
            @Parameter(description = "Policy ID", example = "1", required = true) @PathVariable Long id) {
        PolicyResponse response = policyService.getById(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Policy retrieved successfully", response));
    }

    @GetMapping("/all")
    @Operation(summary = "List all policies (paginated)", description = "Returns a paginated list of all policies. Use page and pageSize to navigate.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of policies returned",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Policies retrieved successfully\",\"data\":{\"content\":[{\"id\":1,\"policyNumber\":\"POL-2025-00001\",\"customerName\":\"John Smith\",\"customerEmail\":\"john.smith@example.com\",\"policyType\":\"HEALTH\",\"coverageAmount\":100000.00,\"premiumAmount\":150.50,\"startDate\":\"2025-01-01\",\"endDate\":\"2026-01-01\"}],\"pagination\":{\"page\":1,\"size\":10,\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true,\"empty\":false}}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/policy/all\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<PageResponse<PolicyResponse>>> getAllPoliciesPaginated(
            @Parameter(description = "Page number (1-based)", example = "1") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(required = false, defaultValue = "10") int pageSize) {
        Pageable pageable = PaginationUtils.createPageable(page, pageSize);
        PageResponse<PolicyResponse> policesResponse = policyService.getAllPolicies(pageable);
        return ResponseEntity.ok(ApiResponseDTO.success("Policies retrieved successfully",policesResponse));
    }

    @PutMapping("/{id}/renew")
    @Operation(summary = "Renew a policy", description = "Renews an existing policy. Typically extends the end date by the policy term.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy renewed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Policy renewed successfully!\",\"data\":{\"id\":1,\"policyNumber\":\"POL-2025-00001\",\"customerName\":\"John Smith\",\"customerEmail\":\"john.smith@example.com\",\"policyType\":\"HEALTH\",\"coverageAmount\":100000.00,\"premiumAmount\":150.50,\"startDate\":\"2026-01-01\",\"endDate\":\"2027-01-01\"}}}"))),
            @ApiResponse(responseCode = "404", description = "Policy not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Policy not found with id: 999\",\"path\":\"/policy/999/renew\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g. business rule). Response contains message only, no fieldErrors in details.",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Policy cannot be renewed in current state\",\"path\":\"/policy/1/renew\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/policy/1/renew\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<PolicyResponse>> renewPolicy(
            @Parameter(description = "Policy ID to renew", example = "1", required = true) @PathVariable Long id) {
        PolicyResponse response = policyService.renewPolicy(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Policy renewed successfully!", response));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a policy", description = "Cancels an active policy. The policy status is updated to cancelled.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy cancelled successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(value = "{\"status\":\"SUCCESS\",\"message\":\"Policy cancelled successfully!\",\"data\":null}"))),
            @ApiResponse(responseCode = "404", description = "Policy not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Policy not found with id: 999\",\"path\":\"/policy/999/cancel\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g. business rule). Response contains message only, no fieldErrors in details.",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Policy cannot be cancelled in current state\",\"path\":\"/policy/1/cancel\",\"traceId\":null,\"details\":{}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2025-02-09T12:00:00Z\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Unexpected error\",\"path\":\"/policy/1/cancel\",\"traceId\":null,\"details\":{}}")))
    })
    public ResponseEntity<ApiResponseDTO<PolicyResponse>> cancelPolicy(
            @Parameter(description = "Policy ID to cancel", example = "1", required = true) @PathVariable Long id) {
        PolicyResponse response = policyService.cancelPolicy(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Policy cancelled successfully!", response));
    }
}
