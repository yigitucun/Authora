package com.authora.authorization.server.authentication.validation;

import com.authora.authorization.server.authentication.dto.SignUpRequest;
import com.authora.authorization.server.user.mapper.UserMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, SignUpRequest> {

    private final UserMapper userMapper;

    @Override
    public boolean isValid(SignUpRequest request, ConstraintValidatorContext context) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return true;
        }

        boolean exists = userMapper.existsByEmail(request.getEmail());
        if (exists) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("email")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}