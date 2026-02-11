package com.rubin.insurance.policy_management_service.dto.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class EmailValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private EmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    @Test
    void testIsValid_ValidSimpleEmail_ReturnsTrue() {
        String email = "john@example.com";

        boolean result = validator.isValid(email, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_ValidEmailWithNumbers_ReturnsTrue() {
        String email = "user123@example.com";

        boolean result = validator.isValid(email, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_ValidEmailWithDots_ReturnsTrue() {
        String email = "john.smith@example.com";

        boolean result = validator.isValid(email, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_ValidEmailWithPlus_ReturnsTrue() {
        String email = "john+test@example.com";

        boolean result = validator.isValid(email, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_ValidEmailWithUnderscore_ReturnsTrue() {
        String email = "john_smith@example.com";

        boolean result = validator.isValid(email, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_ValidEmailWithHyphenInDomain_ReturnsTrue() {
        String email = "john@my-company.com";

        boolean result = validator.isValid(email, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_ValidEmailWithSubdomain_ReturnsTrue() {
        String email = "john@mail.example.com";

        boolean result = validator.isValid(email, context);

        assertTrue(result);
    }


    @Test
    void testIsValid_MissingAtSymbol_ReturnsFalse() {
        // Arrange
        String email = "johnexample.com";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_MissingDomain_ReturnsFalse() {
        // Arrange
        String email = "john@";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_MissingUsername_ReturnsFalse() {
        // Arrange
        String email = "@example.com";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_MissingTopLevelDomain_ReturnsFalse() {
        // Arrange
        String email = "john@example";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_MultipleAtSymbols_ReturnsFalse() {
        // Arrange
        String email = "john@@example.com";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_SpacesInEmail_ReturnsFalse() {
        // Arrange
        String email = "john smith@example.com";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_SpecialCharactersInDomain_ReturnsFalse() {
        // Arrange
        String email = "john@exa$mple.com";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_DotAtStart_ReturnsFalse() {
        // Arrange
        String email = ".john@example.com";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValid_SingleCharacterUsername_ReturnsTrue() {
        // Arrange
        String email = "a@example.com";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsValid_InvalidFormat_ReturnsFalse() {
        // Arrange
        String email = "not_an_email";

        // Act
        boolean result = validator.isValid(email, context);

        // Assert
        assertFalse(result);
    }
}
