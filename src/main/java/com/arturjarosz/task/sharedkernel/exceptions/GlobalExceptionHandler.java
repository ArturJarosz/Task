package com.arturjarosz.task.sharedkernel.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

@ControllerAdvice
public class GlobalExceptionHandler {

    public static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String UNEXPECTED_ERROR = "Exception.unexpected";
    private final MessageSource messageSource;

    @Autowired
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ErrorMessage> handleException(IllegalArgumentException exception) {
        LOG.error(exception.getMessage(), exception);
        String errorMessage = exception.getMessage();
        String message = this.messageSource
                .getMessage(errorMessage, exception.getMessageParameters(), errorMessage,
                        Locale.getDefault());
        return new ResponseEntity<ErrorMessage>(new ErrorMessage(message),
                HttpStatus.BAD_REQUEST);
    }
}
