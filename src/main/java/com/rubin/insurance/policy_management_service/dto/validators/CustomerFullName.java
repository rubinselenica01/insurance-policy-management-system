package com.rubin.insurance.policy_management_service.dto.validators;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {CustomerFullNameValidator.class})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomerFullName {

    String message() default "Full name must contain at least first and last name, letters only";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minParts() default 2;
    int maxLength() default 80;

}
