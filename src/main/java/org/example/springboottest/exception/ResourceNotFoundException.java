package org.example.springboottest.exception;

/**
 * Custom exception type that gets handled by GlobalExceptionHandler, which has
 * the annotation @RestControllerAdvice.  A specific method in that class
 * declares that it handles this exception with @ExceptionHandler(ResourceNotFoundException.class).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
