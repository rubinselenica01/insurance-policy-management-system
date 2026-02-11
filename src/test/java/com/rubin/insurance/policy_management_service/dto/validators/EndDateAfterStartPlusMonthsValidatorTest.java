package com.rubin.insurance.policy_management_service.dto.validators;

import com.rubin.insurance.policy_management_service.dto.PolicyRequest;
import com.rubin.insurance.policy_management_service.model.policy.PolicyType;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class EndDateAfterStartPlusMonthsValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @Mock
    private ValidDateRange annotation;

    private EndDateAfterStartPlusMonthsValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EndDateAfterStartPlusMonthsValidator();
        
        lenient().when(annotation.startField()).thenReturn("startDate");
        lenient().when(annotation.endField()).thenReturn("endDate");
        lenient().when(annotation.minMonths()).thenReturn(6);
        lenient().when(annotation.allowEqualAtMin()).thenReturn(true);
        
        validator.initialize(annotation);

        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    @Test
    void testIsValid_ExactlySixMonthsApart_ReturnsTrue() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 7, 1); // Exactly 6 months later

        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                start,
                end
        );

        boolean result = validator.isValid(request, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_MoreThanSixMonthsApart_ReturnsTrue() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31); // 12 months later

        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                start,
                end
        );

        boolean result = validator.isValid(request, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_EndDateBeforeStartDate_ReturnsFalse() {
        LocalDate start = LocalDate.of(2024, 6, 1);
        LocalDate end = LocalDate.of(2024, 1, 1); // Before start date

        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                start,
                end
        );

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }

    @Test
    void testIsValid_EndDateLessThanSixMonthsAfterStart_ReturnsFalse() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 5, 1); // Only 4 months later

        PolicyRequest request = new PolicyRequest(
                "John Smith",
                "john@example.com",
                PolicyType.HEALTH,
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                start,
                end
        );

        boolean result = validator.isValid(request, context);

        assertFalse(result);
    }
}
