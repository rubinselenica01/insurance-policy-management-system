package com.rubin.insurance.policy_management_service.controller;


import com.rubin.insurance.policy_management_service.dto.ApiResponseDTO;
import com.rubin.insurance.policy_management_service.dto.PageResponse;
import com.rubin.insurance.policy_management_service.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.dto.PolicyResponse;
import com.rubin.insurance.policy_management_service.mapper.PolicyMapper;
import com.rubin.insurance.policy_management_service.model.policy.Policy;
import com.rubin.insurance.policy_management_service.service.PolicyService;
import com.rubin.insurance.policy_management_service.utils.PaginationUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/policy")
@Slf4j
public class PolicyController {

    private final PolicyService policyService;

    @Autowired
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/create-new")
    public ResponseEntity<ApiResponseDTO<PolicyResponse>> createPolicy(@Valid @RequestBody PolicyRequest policyRequest) {
        PolicyResponse response = policyService.savePolicy(policyRequest);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Policy created successfully", response)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<PolicyResponse>> getPolicy(@PathVariable Long id) {
        PolicyResponse response = policyService.getById(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Policy retrieved successfully",response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponseDTO<PageResponse<PolicyResponse>>> getAllPoliciesPaginated(
                                                                                @RequestParam(required = false, defaultValue = "1") int page,
                                                                                @RequestParam(required = false, defaultValue = "10") int pageSize){
        Pageable pageable = PaginationUtils.createPageable(page, pageSize);
        Page<PolicyResponse> policesResponse = policyService.getAllPolicies(pageable);
        return ApiResponseDTO.success(policesResponse, "Policies retrieved successfully");
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<ApiResponseDTO<PolicyResponse>> renewPolicy(@PathVariable Long id) {
        PolicyResponse response = policyService.renewPolicy(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Policy renewed successfully!", response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponseDTO<Void>> cancelPolicy(@PathVariable Long id) {
        policyService.cancelPolicy(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Policy cancelled successfully!", null));
    }


}
