package com.rubin.insurance.policy_management_service.dto.validators;

import com.rubin.insurance.policy_management_service.dto.PolicyRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PremiumAmountValidator implements ConstraintValidator<PremiumAmount, PolicyRequest> {

    @Override
    public boolean isValid(PolicyRequest req, ConstraintValidatorContext ctx) {
        if (req == null) return true;

        if (req.coverageAmount() == null || req.premiumAmount() == null) return true;

        if (req.coverageAmount().compareTo(req.premiumAmount()) <= 0) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Coverage Amount must be greater than Premium Amount")
               .addPropertyNode("coverageAmount")
               .addConstraintViolation();
            return false;
        }

        return true;
    }
}
