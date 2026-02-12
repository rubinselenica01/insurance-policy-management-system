package com.rubin.insurance.policy_management_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rubin.insurance.policy_management_service.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.dto.PolicyResponse;
import com.rubin.insurance.policy_management_service.model.policy.PolicyType;
import com.rubin.insurance.policy_management_service.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreatePolicy_Success() throws Exception {
        // Arrange
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john.smith@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        PolicyResponse response = new PolicyResponse(
                1L,
                "POL-2024-000001",
                "John Smith",
                "john.smith@example.com",
                "HEALTH",
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "ACTIVE"
        );

        when(policyService.savePolicy(any(PolicyRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Policy created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.policyNumber").value("POL-2024-000001"))
                .andExpect(jsonPath("$.data.customerName").value("John Smith"))
                .andExpect(jsonPath("$.data.customerEmail").value("john.smith@example.com"))
                .andExpect(jsonPath("$.data.policyType").value("HEALTH"))
                .andExpect(jsonPath("$.data.coverageAmount").value(100000.00))
                .andExpect(jsonPath("$.data.premiumAmount").value(500.00))
                .andExpect(jsonPath("$.data.policyStatus").value("ACTIVE"));

        verify(policyService, times(1)).savePolicy(any(PolicyRequest.class));
    }

    @Test
    void testCreatePolicy_InvalidEmail() throws Exception {
        // Arrange - Invalid email format
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "invalid-email",  // Invalid email
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()) // Validation errors return 409
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_InvalidCustomerName() throws Exception {
        // Arrange - Single name (needs at least first and last)
        PolicyRequest request = new PolicyRequest(
                "John",  // Only first name
                "john.smith@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_MissingRequiredFields() throws Exception {
        // Arrange - Missing customerName
        String invalidJson = """
                {
                    "customerEmail": "john.smith@example.com",
                    "policyType": "HEALTH",
                    "coverageAmount": 100000.00,
                    "premiumAmount": 500.00,
                    "startDate": "2024-01-01",
                    "endDate": "2024-12-31"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_InvalidPolicyType() throws Exception {
        // Arrange - Invalid policy type
        String invalidJson = """
                {
                    "customerName": "John Smith",
                    "customerEmail": "john.smith@example.com",
                    "policyType": "INVALID_TYPE",
                    "coverageAmount": 100000.00,
                    "premiumAmount": 500.00,
                    "startDate": "2024-01-01",
                    "endDate": "2024-12-31"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_NegativeCoverageAmount() throws Exception {
        // Arrange - Negative coverage amount
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john.smith@example.com",
                PolicyType.HEALTH,
                new BigDecimal("-100000.00"),  // Negative
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_PremiumGreaterThanCoverage() throws Exception {
        // Arrange - Premium > Coverage (violates @PremiumAmount validator)
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john.smith@example.com",
                PolicyType.HEALTH,
                new BigDecimal("500.00"),      // Coverage
                new BigDecimal("1000.00"),     // Premium > Coverage
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_InvalidDateRange_LessThanSixMonths() throws Exception {
        // Arrange - End date less than 6 months after start
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john.smith@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 5, 1)  // Only 4 months later
        );

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_EndDateBeforeStartDate() throws Exception {
        // Arrange - End date before start date
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john.smith@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 12, 31),
                LocalDate.of(2024, 1, 1)  // Before start date
        );

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_MalformedJson() throws Exception {
        // Arrange - Malformed JSON
        String malformedJson = "{invalid json}";

        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(policyService, never()).savePolicy(any());
    }

    @Test
    void testCreatePolicy_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/policy/create-new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(policyService, never()).savePolicy(any());
    }
}
