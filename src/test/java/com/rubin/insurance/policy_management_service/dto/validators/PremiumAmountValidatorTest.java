package com.rubin.insurance.policy_management_service.dto.validators;

import com.rubin.insurance.policy_management_service.model.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.model.dto.validators.PremiumAmountValidator;
import com.rubin.insurance.policy_management_service.model.entity.policy.PolicyType;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PremiumAmountValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    private PremiumAmountValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PremiumAmountValidator();

        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }


    @Test
    void testIsValid_CoverageGreaterThanPremium_ReturnsTrue() {
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        boolean result = validator.isValid(request, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_CoverageEqualToPremium_ReturnsFalse() {
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("500.00"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }

    @Test
    void testIsValid_CoverageLessThanPremium_ReturnsFalse() {
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }

    @Test
    void testIsValid_SlightlyHigherCoverage_ReturnsTrue() {
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("500.01"),
                new BigDecimal("500.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        boolean result = validator.isValid(request, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_LargeDifference_ReturnsTrue() {
        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("1000000.00"),
                new BigDecimal("100.00"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        boolean result = validator.isValid(request, context);

        assertTrue(result);
    }
}
