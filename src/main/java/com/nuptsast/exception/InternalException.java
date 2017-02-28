package com.nuptsast.exception;

/**
 * Created by zheng on 2016/11/24.
 * For fit-jpa.
 */
public class InternalException extends RuntimeException {
  public InternalException() {
    super();
  }
  public InternalException(Throwable cause) {
    super(cause);
  }
  public InternalException(String message, Throwable cause) {
    super(message, cause);
  }
}
