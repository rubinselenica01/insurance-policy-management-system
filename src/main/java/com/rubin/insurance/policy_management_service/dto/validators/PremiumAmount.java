package com.rubin.insurance.policy_management_service.dto.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PremiumAmountValidator.class)
public @interface PremiumAmount {

    String message() default "Invalid range";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}