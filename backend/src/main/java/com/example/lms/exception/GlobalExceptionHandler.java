package com.example.lms.exception;

import com.example.lms.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(LMSException.class)
  public ResponseEntity<ErrorResponse> handleLMSException(LMSException ex, HttpServletRequest request) {
    logException(ex, request);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(ex.getStatus().value())
        .error(ex.getStatus().getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .errorCode(ex.getErrorCode())
        .build();
    
    return new ResponseEntity<>(errorResponse, ex.getStatus());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
    logException(ex, request);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.NOT_FOUND.value())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .errorCode(ex.getErrorCode())
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
    logException(ex, request);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .errorCode(ex.getErrorCode())
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
    logException(ex, request);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.CONFLICT.value())
        .error(HttpStatus.CONFLICT.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .errorCode(ex.getErrorCode())
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex, HttpServletRequest request) {
    logException(ex, request);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.NOT_FOUND.value())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message("Requested resource not found")
        .path(request.getRequestURI())
        .errorCode("RESOURCE_NOT_FOUND")
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
    logException(ex, request);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .errorCode("INVALID_ARGUMENT")
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ClassCastException.class)
  public ResponseEntity<ErrorResponse> handleClassCastException(ClassCastException ex, HttpServletRequest request) {
    logException(ex, request);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Invalid data type in request. Please check the request format.")
        .path(request.getRequestURI())
        .errorCode("TYPE_MISMATCH")
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    logException(ex, request);
    
    String message = String.format("Parameter '%s' should be of type '%s'", 
        ex.getName(), 
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(message)
        .path(request.getRequestURI())
        .errorCode("TYPE_MISMATCH")
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
    logException(ex, request);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Malformed JSON request. Please check the request body format.")
        .path(request.getRequestURI())
        .errorCode("MALFORMED_REQUEST")
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
    logException(ex, request);
    
    Map<String, Object> validationErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> 
        validationErrors.put(error.getField(), error.getDefaultMessage())
    );
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Validation failed for one or more fields")
        .path(request.getRequestURI())
        .errorCode("VALIDATION_ERROR")
        .details(validationErrors)
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
    logException(ex, request);
    
    String message = "Database constraint violation. ";
    if (ex.getMessage().contains("unique") || ex.getMessage().contains("duplicate")) {
      message += "The resource already exists.";
    } else if (ex.getMessage().contains("foreign key")) {
      message += "Referenced resource does not exist.";
    } else {
      message += "Data integrity constraint violated.";
    }
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.CONFLICT.value())
        .error(HttpStatus.CONFLICT.getReasonPhrase())
        .message(message)
        .path(request.getRequestURI())
        .errorCode("DATA_INTEGRITY_VIOLATION")
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception occurred", ex);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .correlationId(getCorrelationId())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("An unexpected error occurred. Please try again later.")
        .path(request.getRequestURI())
        .errorCode("INTERNAL_SERVER_ERROR")
        .build();
    
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private String getCorrelationId() {
    String correlationId = MDC.get("correlationId");
    return correlationId != null ? correlationId : "N/A";
  }

  private void logException(Exception ex, HttpServletRequest request) {
    String endpoint = request.getMethod() + " " + request.getRequestURI();
    
    if (ex instanceof LMSException) {
      LMSException lmsEx = (LMSException) ex;
      if (lmsEx.getStatus().is5xxServerError()) {
        log.error("[{}] {} - {}", endpoint, lmsEx.getErrorCode(), lmsEx.getMessage(), ex);
      } else {
        log.warn("[{}] {} - {}", endpoint, lmsEx.getErrorCode(), lmsEx.getMessage());
      }
    } else if (ex instanceof NoSuchElementException || ex instanceof IllegalArgumentException) {
      log.warn("[{}] {} - {}", endpoint, ex.getClass().getSimpleName(), ex.getMessage());
    } else {
      log.error("[{}] {} - {}", endpoint, ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }
  }
}
