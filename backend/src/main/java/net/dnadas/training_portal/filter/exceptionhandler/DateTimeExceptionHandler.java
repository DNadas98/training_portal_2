package net.dnadas.training_portal.filter.exceptionhandler;

import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.exception.utils.datetime.DateTimeBadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class DateTimeExceptionHandler {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @ExceptionHandler(DateTimeBadRequestException.class)
  public ResponseEntity<?> handleDateTimeException(DateTimeBadRequestException e, Locale locale) {
    logger.error("DateTimeBadRequestException - " + e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
  }
}
