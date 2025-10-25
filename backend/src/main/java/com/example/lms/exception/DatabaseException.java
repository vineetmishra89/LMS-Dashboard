package com.example.lms.exception;

import org.springframework.http.HttpStatus;

public class DatabaseException extends LMSException {
  public DatabaseException(String message) {
    super(message, HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_ERROR");
  }

  public DatabaseException(String message, Throwable cause) {
    super(message, cause, HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_ERROR");
  }
}
