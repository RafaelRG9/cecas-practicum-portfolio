package edu.franklin.cecas.exception;

import java.util.List;

import edu.franklin.cecas.seed.SeedValidationError;

public class SeedValidationException extends RuntimeException {
    private final List<SeedValidationError> errors;

    public SeedValidationException(List<SeedValidationError> errors) {
        super("Seed validation failed.");
        this.errors = List.copyOf(errors);
    }

    public List<SeedValidationError> getErrors() {
        return errors;
    }
}
