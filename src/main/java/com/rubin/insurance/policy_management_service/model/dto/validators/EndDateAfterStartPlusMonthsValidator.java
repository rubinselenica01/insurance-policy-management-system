package com.rubin.insurance.policy_management_service.model.dto.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class EndDateAfterStartPlusMonthsValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startField;
    private String endField;
    private int minMonths;
    private boolean allowEqualAtMin;

    @Override
    public void initialize(ValidDateRange a) {
        this.startField = a.startField();
        this.endField = a.endField();
        this.minMonths = a.minMonths();
        this.allowEqualAtMin = a.allowEqualAtMin();
    }

    @Override
    public boolean isValid(Object target, ConstraintValidatorContext ctx) {
        if (target == null) return true;

        try {
            LocalDate start = readLocalDate(target, startField);
            LocalDate end = readLocalDate(target, endField);

            if (start == null || end == null) return true;

            boolean valid = true;
            ctx.disableDefaultConstraintViolation();


            LocalDate minEnd = start.plusMonths(minMonths);

            boolean meetsMin = allowEqualAtMin ? !end.isBefore(minEnd) : end.isAfter(minEnd);

            if (!end.isAfter(start) || !meetsMin) {
                ctx.buildConstraintViolationWithTemplate(endField + " must be at least " + minMonths + " months after " + startField)
                        .addPropertyNode(endField)
                        .addConstraintViolation();
                valid = false;
            }

            return valid;

        } catch (Exception e) {
            // Misconfigured fields or wrong types
            return false;
        }
    }

    private LocalDate readLocalDate(Object obj, String fieldName) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        Object v = f.get(obj);
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        throw new IllegalArgumentException("Field " + fieldName + " must be LocalDate");
    }
}
