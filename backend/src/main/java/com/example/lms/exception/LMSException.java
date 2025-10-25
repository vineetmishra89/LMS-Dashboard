package com.example.lms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class LMSException extends RuntimeException {
  private final HttpStatus status;
  private final String errorCode;

  protected LMSException(String message, HttpStatus status, String errorCode) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }

  protected LMSException(String message, Throwable cause, HttpStatus status, String errorCode) {
    super(message, cause);
    this.status = status;
    this.errorCode = errorCode;
  }
}
