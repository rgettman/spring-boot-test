package org.example.springboottest.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * If an exception is thrown or bubbled up by a Controller, use a class with
 * the annotation @RestControllerAdvice to catch it and execute a specific
 * handler method depending on the type of exception thrown.
 * Lombok's @Slf4j automatically declares the Logger object below!
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle ResourceNotFoundException.  Here, this generates a 404 error response.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException e) {
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle NoResourceFoundException, which Spring throws when a request matches
     * no controller method and no static resource.  Here, this generates a 404 error response.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle BadParameterException and IllegalArgumentException.  Here, this generates a 400 error response.
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadParameter(RuntimeException e) {
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MethodArgumentNotValidException, which Spring throws when a @Valid
     * request body breaks a Bean Validation constraint.  Without this handler,
     * the Exception.class fallback below would turn it into a 500.  Here, this
     * generates a 400 error response listing every violated field.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * As a fallback, include this to handle any unexpected exception, which
     * generates a 500 response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred.");

        log.error("Unexpected error of type {} caught: {}", e.getClass().getName(), e.getMessage(), e);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
