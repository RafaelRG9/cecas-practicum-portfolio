package edu.franklin.cecas.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import edu.franklin.cecas.exception.EmailAlreadyExistsException;
import edu.franklin.cecas.exception.InvalidCredentialsException;
import edu.franklin.cecas.exception.InvalidPasswordException;
import edu.franklin.cecas.exception.PasswordChangeNotRequiredException;
import edu.franklin.cecas.exception.PasswordMismatchException;
import edu.franklin.cecas.exception.RegistrationNotAllowedException;
import edu.franklin.cecas.exception.UnauthorizedRoleException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Email already exists");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", "AUTH_EMAIL_EXISTS");
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Invalid Credentials");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", "AUTH_INVALID_CREDENTIALS");
        return problem;
    }

    @ExceptionHandler(RegistrationNotAllowedException.class)
    public ProblemDetail handleRegistrationNotAllowed(RegistrationNotAllowedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Registration not allowed");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", "AUTH_REGISTRATION_NOT_ALLOWED");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setDetail("One or more request fields are invalid.");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        problem.setProperty("errorCode", "VALIDATION_FAILED");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setDetail("You do not have permission to access this resource.");
        problem.setProperty("errorCode", "ACCESS_DENIED");
        return problem;
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPassword(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid Password");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", "PASSWORD_INVALID");
        return problem;
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ProblemDetail handlePasswordMismatch(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Password Mismatch");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", "PASSWORD_MISMATCH");
        return problem;
    }

    @ExceptionHandler(PasswordChangeNotRequiredException.class)
    public ProblemDetail handleNotRequired(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Password Change Not Required");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", "PASSWORD_CHANGE_NOT_REQUIRED");
        return problem;
    }

    @ExceptionHandler(UnauthorizedRoleException.class)
    public ProblemDetail handleUnauthorizedRole(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Unauthorized Role");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", "UNAUTHORIZED_ROLE");
        return problem;
    }

    /*
     * Generic exception handler to catch any unhandled exceptions and return a
     * standardized error response.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred.");
        return problem;
    }
}
