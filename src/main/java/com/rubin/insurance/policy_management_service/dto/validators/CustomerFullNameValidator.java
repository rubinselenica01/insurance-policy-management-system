package com.rubin.insurance.policy_management_service.dto.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class CustomerFullNameValidator implements ConstraintValidator<CustomerFullName, String> {

    private int minParts;
    private int maxLength;

    // Allows letters with accents, spaces, hyphen, apostrophe.
    // \p{L} = any unicode letter (good for international names)
    private static final Pattern ALLOWED = Pattern.compile("^[\\p{L}]+([\\p{L}'-]*)([ ]+[\\p{L}]+([\\p{L}'-]*))*$");

    @Override
    public void initialize(CustomerFullName annotation) {
        this.minParts = annotation.minParts();
        this.maxLength = annotation.maxLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;

        String v = value.trim();

        if (v.isEmpty()) return true;
        if (v.length() > maxLength) return false;

        // Normalize multiple spaces
        v = v.replaceAll("\\s+", " ");

        // Must match allowed characters/pattern
        if (!ALLOWED.matcher(v).matches()) return false;

        // Must contain at least N parts (first + last)
        String[] parts = v.split(" ");
        return parts.length >= minParts;
    }
}
