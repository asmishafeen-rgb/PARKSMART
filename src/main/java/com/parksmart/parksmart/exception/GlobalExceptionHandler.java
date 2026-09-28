package com.parksmart.parksmart.exception;

import com.parksmart.parksmart.dto.ErrorResponse;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({SlotAlreadyBookedException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({InvalidBookingException.class, InvalidCheckInException.class, InvalidCheckOutException.class,
            IllegalArgumentException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception exception) {
        String message = exception instanceof HttpMessageNotReadableException || exception instanceof MethodArgumentTypeMismatchException
                ? "Request contains an invalid value or date-time format. Use ISO format such as 2026-09-28T10:00:00."
                : Objects.requireNonNullElse(exception.getMessage(), "The request is invalid.");
        return response(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return response(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> internal(Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.");
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message));
    }
}
