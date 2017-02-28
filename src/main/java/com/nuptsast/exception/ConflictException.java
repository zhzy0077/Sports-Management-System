package com.nuptsast.exception;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public class ConflictException extends RuntimeException {
  public ConflictException() {
  }

  public ConflictException(String message) {
    super(message);
  }
}
