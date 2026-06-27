package edu.franklin.cecas.exception;

public class PasswordChangeNotRequiredException extends RuntimeException {
    public PasswordChangeNotRequiredException(String message){
        super(message);
    }
}