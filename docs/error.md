# Error Handling

Errors happen.  Outside systems fail, interfaces fail, things aren't found, and other
things happen.  Things don't _usually_ happen, but they can happen and they will.  This
is how such errors are handled in Spring Boot.

## ErrorResponse Object

The `ErrorResponse` object is a simple POJO that represents a response sent back to the
client when an error occurs.  It has a `LocalDateTime` for the timestamp of the error,
an integer status for the HTTP response code, and a message.

## Custom Exceptions

The `ResourceNotFoundException` class is a `RuntimeException` that represents when a
valid path is reached, but it asks for a resource that isn't found.  Here, an employee
not being found throws this exception.

## Global Exception Handler

The `GlobalExceptionHandler` class is annotated with `@RestControllerAdvice`.  Spring
looks for classes with that annotation to look for methods to handle exceptions thrown
by Controller methods.  Because they subclass `RunTimeException`, Controller methods
don't need a `try` block or a `throws` clause.

Each method in this class is annotated with `@ExceptionHandler`, indicating which
exception it handles.  These methods return a `ResponseEntity<ErrorResponse>`.

- `ResourceNotFoundException` - Return a 404 Not Found response with the custom message.
- `NoResourceFoundException` - A Spring exception for when it cannot find a Controller
to handle the request.  Returns a generic 404 Not Found response.
- `HttpMessageNotReadableException`, `IllegalArgumentException` - One method handles
both exceptions by passing them both to `@ExceptionHandler`.  Returns a 400 Bad Request
response.
- `MethodArgumentNotValidException` - A Spring Validation exception for when a bean
validation constraint is violated.  Returns a 400 Bad Request response listing the
validation rule(s) broken.
- `Exception` - Always include a handler for the broadest exception, to control the
response and avoid dumping a very user-unfriendly stack trace to the user.  Returns a
500 Internal Server Error response.