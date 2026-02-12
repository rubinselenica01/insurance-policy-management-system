package com.rubin.insurance.policy_management_service.dto.validators;

import com.rubin.insurance.policy_management_service.model.dto.validators.CustomerFullName;
import com.rubin.insurance.policy_management_service.model.dto.validators.CustomerFullNameValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CustomerFullNameValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private CustomerFullName annotation;

    private CustomerFullNameValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CustomerFullNameValidator();

        // Setup default annotation values
        lenient().when(annotation.minParts()).thenReturn(2);
        lenient().when(annotation.maxLength()).thenReturn(80);

        validator.initialize(annotation);
    }

    @Test
    void testIsValid_ValidTwoPartName_ReturnsTrue() {
        String name = "John Smith";

        boolean result = validator.isValid(name, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_ValidThreePartName_ReturnsTrue() {
        String name = "John Michael Smith";

        boolean result = validator.isValid(name, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_NameWithHyphen_ReturnsTrue() {
        String name = "Mary-Jane Smith";

        boolean result = validator.isValid(name, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_NameWithApostrophe_ReturnsTrue() {
        String name = "John O'Brien";

        boolean result = validator.isValid(name, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_NameWithAccents_ReturnsTrue() {
        String name = "José García";

        boolean result = validator.isValid(name, context);

        assertTrue(result);

    }


    @Test
    void testIsValid_LongValidName_ReturnsTrue() {
        // Arrange - 75 characters (under 80 limit)
        String name = "Alexander Montgomery Worthington III";

        boolean result = validator.isValid(name, context);

        assertTrue(result);

    }

    @Test
    void testIsValid_SingleName_ReturnsFalse() {
        String name = "John";

        boolean result = validator.isValid(name, context);

        assertFalse(result);


    }

    @Test
    void testIsValid_NameWithNumbers_ReturnsFalse() {
        String name = "John Smith123";

        boolean result = validator.isValid(name, context);

        assertFalse(result);


    }

    @Test
    void testIsValid_NameWithSpecialCharacters_ReturnsFalse() {
        String name = "John @Smith";

        boolean result = validator.isValid(name, context);

        assertFalse(result);


    }

    @Test
    void testIsValid_NameTooLong_ReturnsFalse() {
        // Arrange - More than 80 characters
        String name = "A".repeat(50) + " " + "B".repeat(50);

        boolean result = validator.isValid(name, context);

        assertFalse(result);


    }

    @Test
    void testIsValid_NameWithUnderscores_ReturnsFalse() {
        String name = "John_Smith";

        boolean result = validator.isValid(name, context);

        assertFalse(result);


    }
}
