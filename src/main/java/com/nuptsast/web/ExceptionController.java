package com.nuptsast.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuptsast.exception.ConflictException;
import com.nuptsast.exception.ForbiddenAccessException;
import com.nuptsast.exception.InternalException;
import com.nuptsast.exception.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * Created by zheng on 2016/11/19.
 * For fit-jpa.
 */
@RestController
@ControllerAdvice
public class ExceptionController {
  private final Log logger = LogFactory.getLog(this.getClass());

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler({
                        NullPointerException.class,
                        DataIntegrityViolationException.class,
                        IllegalArgumentException.class,
                        UsernameNotFoundException.class
  })
  public String nullPointer(Exception e) {
    logger.error("error", e);
    return errProduce(e.getMessage());
  }

  @ResponseStatus(value = HttpStatus.FORBIDDEN)
  @ExceptionHandler({ ForbiddenAccessException.class })
  public void forbidden() {

  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler({ NotFoundException.class })
  public String notFoundException(Exception e) {
    logger.error("error", e);
    return errProduce(e.getMessage());
  }

  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler({InternalException.class})
  public void internal(Exception e) {
    logger.error("error", e);
  }

  @ResponseStatus(value = HttpStatus.CONFLICT)
  @ExceptionHandler({ ConflictException.class })
  public String conflictException(Exception e) {
    return errProduce(e.getMessage());
  }

  private String errProduce(String err) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(Collections.singletonMap("message", err));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      // won't happen
    }
    return null;
  }
}
