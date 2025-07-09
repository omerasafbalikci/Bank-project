package org.bank.account.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.bank.account.utilities.exceptions.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(AccountBalanceException.class)
    public ResponseEntity<Object> handleAccountBalanceException(AccountBalanceException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFoundException(AccountNotFoundException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(JsonProcessException.class)
    public ResponseEntity<Object> handleJsonProcessException(JsonProcessException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "JSON processing error: " + exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(RocksDbException.class)
    public ResponseEntity<Object> handleRocksDbException(RocksDbException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "RocksDB error: " + exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<Object> handleUnexpectedException(UnexpectedException exception, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + exception.getMessage(), request.getRequestURI());
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException exception,
                                                               @NonNull HttpHeaders headers,
                                                               @NonNull HttpStatusCode statusCode,
                                                               @NonNull WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed");
        errorResponse.addValidationError(fieldErrors);
        errorResponse.setPath(servletWebRequest.getRequest().getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException exception,
                                                               @NonNull HttpHeaders headers,
                                                               @NonNull HttpStatusCode statusCode,
                                                               @NonNull WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request", exception);
        errorResponse.setPath(servletWebRequest.getRequest().getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message, String path) {
        ErrorResponse errorResponse = new ErrorResponse(status, message);
        errorResponse.setPath(path);
        return new ResponseEntity<>(errorResponse, status);
    }
}
