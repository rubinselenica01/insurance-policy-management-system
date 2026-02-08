package com.rubin.insurance.policy_management_service.dto.validators;

import com.rubin.insurance.policy_management_service.dto.PolicyRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PremiumAmountValidator implements ConstraintValidator<PremiumAmount, PolicyRequest> {

    @Override
    public boolean isValid(PolicyRequest req, ConstraintValidatorContext ctx) {
        if (req == null) return true;

        if (req.getCoverageAmount() == null || req.getPremiumAmount() == null) return true;

        if (req.getCoverageAmount().compareTo(req.getPremiumAmount()) <= 0) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Coverage Amount must be greater than Premium Amount")
               .addPropertyNode("coverageAmount")
               .addConstraintViolation();
            return false;
        }

        return true;
    }
}
