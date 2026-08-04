package org.microsoft.qintelipass.exceptions;

import org.microsoft.qintelipass.dtos.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(LoginLockedException.class)
    public ResponseEntity<Map<String, Object>> handleLoginLockedException(LoginLockedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        body.put("locked", true);
        body.put("retry_after_minutes", ex.getRetryAfterMinutes());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<Map<String, Object>> handleLoginFailedException(LoginFailedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", ex.getMessage());
        if (ex.getAttemptsRemaining() != null) {
            body.put("attempts_remaining", ex.getAttemptsRemaining());
        }
        if (ex.getLocked() != null) {
            body.put("locked", ex.getLocked());
        }
        if (ex.getRetryAfterMinutes() != null) {
            body.put("retry_after_minutes", ex.getRetryAfterMinutes());
        }
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictException(ConflictException exception) {
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.CONFLICT)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().isEmpty()
                ? "Request validation failed."
                : exception.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }
}
