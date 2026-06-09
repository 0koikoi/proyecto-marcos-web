package pe.edu.utp.huellitas.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DniPeruanoValidator implements ConstraintValidator<DniPeruano, String> {

    @Override
    public void initialize(DniPeruano constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        // Valida que sean exactamente 8 números
        return value.matches("^\\d{8}$");
    }
}
