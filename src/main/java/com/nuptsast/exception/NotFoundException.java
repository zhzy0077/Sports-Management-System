package com.nuptsast.exception;

/**
 * Created by zheng on 2016/11/17.
 * For fit-jpa.
 */
public class NotFoundException extends RuntimeException {
  public NotFoundException() {
  }

  public NotFoundException(String message) {
    super(message);
  }
}
