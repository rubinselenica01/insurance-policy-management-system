package com.rubin.insurance.policy_management_service.controller;

import com.rubin.insurance.policy_management_service.dto.ApiResponseDTO;
import com.rubin.insurance.policy_management_service.dto.ClaimRequest;
import com.rubin.insurance.policy_management_service.dto.ClaimResponse;
import com.rubin.insurance.policy_management_service.dto.UpdateClaimStatusDTO;
import com.rubin.insurance.policy_management_service.service.ClaimService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/claim")
@Slf4j
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/create-new")
    public ResponseEntity<ApiResponseDTO<ClaimResponse>> createClaim(@Valid @RequestBody ClaimRequest claimRequest) {
        ClaimResponse result = claimService.createClaim(claimRequest);
        return ResponseEntity.ok(ApiResponseDTO.success("Claim created successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ClaimResponse>> getClaimById(@PathVariable Long id) {
         ClaimResponse result = claimService.getClaimById(id);
         return ResponseEntity.ok(ApiResponseDTO.success("Claim found successfully", result));
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<ApiResponseDTO<List<ClaimResponse>>> getClaimsByPolicyId(@PathVariable Long policyId){
        List<ClaimResponse> resultList = claimService.getClaimsByPolicyId(policyId);
        return ResponseEntity.ok(ApiResponseDTO.success("Claims found successfully", resultList));
    }

    @PatchMapping("/{id}/update-status")
    public ResponseEntity<ApiResponseDTO<ClaimResponse>> updateClaimStatus(@PathVariable Long id, @Valid @RequestBody UpdateClaimStatusDTO updateClaimStatusDTO) {
        ClaimResponse result = claimService.updateStatus(id, updateClaimStatusDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Claim status updated successfully", result));

    }
}
