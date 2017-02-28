package com.nuptsast.exception;

/**
 * Created by zheng on 2016/11/20.
 * For fit-jpa.
 */
public class ForbiddenAccessException extends RuntimeException {
  public ForbiddenAccessException() {
  }

  public ForbiddenAccessException(String message) {
    super(message);
  }
}
