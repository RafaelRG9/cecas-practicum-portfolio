package edu.franklin.cecas.exception;

public class RegistrationNotAllowedException extends RuntimeException {
    public RegistrationNotAllowedException(String message) {
        super(message);
    }
}
