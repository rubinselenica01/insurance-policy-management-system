package com.rubin.insurance.policy_management_service.dto.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndDateAfterStartPlusMonthsValidator.class)
public @interface ValidDateRange {

    String message() default "Invalid date range";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String startField() default "startDate";
    String endField() default "endDate";

    int minMonths() default 6;
    boolean allowEqualAtMin() default true;
}
