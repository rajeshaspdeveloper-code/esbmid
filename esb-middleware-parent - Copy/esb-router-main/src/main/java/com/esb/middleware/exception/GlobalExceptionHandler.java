package com.esb.middleware.exception;

import com.esb.middleware.model.EsbResponse;
import com.esb.plugin.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

/**
 * Global exception handler for ESB Router
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle ESB specific exceptions
     */
    @ExceptionHandler(EsbException.class)
    public ResponseEntity<EsbResponse> handleEsbException(EsbException ex, WebRequest request) {
        logger.error("ESB Exception occurred: {}", ex.getMessage(), ex);
        
        EsbResponse response = EsbResponse.error(
            ex.getRequestId(), 
            ex.getErrorCode(), 
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handle plugin specific exceptions
     */
    @ExceptionHandler(PluginException.class)
    public ResponseEntity<EsbResponse> handlePluginException(PluginException ex, WebRequest request) {
        logger.error("Plugin Exception occurred: pluginId={}, errorCode={}, message={}", 
                   ex.getPluginId(), ex.getErrorCode(), ex.getMessage(), ex);
        
        EsbResponse response = EsbResponse.error(
            "PLUGIN_ERROR", 
            ex.getErrorCode() != null ? ex.getErrorCode() : "PLUGIN_ERROR", 
            ex.getMessage()
        );
        
        if (ex.getPluginId() != null) {
            response.addMetadata("pluginId", ex.getPluginId());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EsbResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        logger.error("Validation Exception occurred: {}", ex.getMessage());
        
        BindingResult bindingResult = ex.getBindingResult();
        String validationErrors = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        EsbResponse response = EsbResponse.error(
            "VALIDATION_ERROR", 
            "INVALID_REQUEST", 
            "Validation failed: " + validationErrors
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EsbResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.error("Illegal Argument Exception occurred: {}", ex.getMessage(), ex);
        
        EsbResponse response = EsbResponse.error(
            "INVALID_ARGUMENT", 
            "BAD_REQUEST", 
            "Invalid argument: " + ex.getMessage()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle timeout exceptions
     */
    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public ResponseEntity<EsbResponse> handleTimeoutException(java.util.concurrent.TimeoutException ex, WebRequest request) {
        logger.error("Timeout Exception occurred: {}", ex.getMessage(), ex);
        
        EsbResponse response = EsbResponse.error(
            "TIMEOUT_ERROR", 
            "REQUEST_TIMEOUT", 
            "Request processing timed out: " + ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
    }
    
    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<EsbResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime Exception occurred: {}", ex.getMessage(), ex);
        
        EsbResponse response = EsbResponse.error(
            "RUNTIME_ERROR", 
            "INTERNAL_ERROR", 
            "Runtime error occurred: " + ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<EsbResponse> handleGeneralException(Exception ex, WebRequest request) {
        logger.error("Unexpected Exception occurred: {}", ex.getMessage(), ex);
        
        EsbResponse response = EsbResponse.error(
            "SYSTEM_ERROR", 
            "INTERNAL_ERROR", 
            "An unexpected error occurred. Please contact support."
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handle null pointer exceptions
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<EsbResponse> handleNullPointerException(NullPointerException ex, WebRequest request) {
        logger.error("Null Pointer Exception occurred: {}", ex.getMessage(), ex);
        
        EsbResponse response = EsbResponse.error(
            "NULL_POINTER_ERROR", 
            "INTERNAL_ERROR", 
            "Null pointer error occurred during processing"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handle security exceptions
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<EsbResponse> handleSecurityException(SecurityException ex, WebRequest request) {
        logger.error("Security Exception occurred: {}", ex.getMessage(), ex);
        
        EsbResponse response = EsbResponse.error(
            "SECURITY_ERROR", 
            "FORBIDDEN", 
            "Security violation: Access denied"
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}

/**
 * Custom ESB Exception class
 */
class EsbException extends Exception {
    
    private String requestId;
    private String errorCode;
    
    public EsbException(String message) {
        super(message);
    }
    
    public EsbException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EsbException(String requestId, String errorCode, String message) {
        super(message);
        this.requestId = requestId;
        this.errorCode = errorCode;
    }
    
    public EsbException(String requestId, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.requestId = requestId;
        this.errorCode = errorCode;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}