package com.ga.equestrian.exception;

import com.ga.equestrian.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class turns exceptions into JSON error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{

    /**
     * handles requests for something that does not exist.
     *
     * @param ex the exception thrown.
     * @param request   the request that caused the error.
     * @return a 404 response with a structured error body.
     */
    @ExceptionHandler(InformationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(InformationNotFoundException ex, HttpServletRequest request){
       return buildResponse(HttpStatus.NOT_FOUND, "INFORMATION_NOT_FOUND", ex.getMessage(), request);
    }

    /**
     * handles requests for conflicts(e.g. registered with same email).
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 409 response with a structured error body.
     */
    @ExceptionHandler(InformationExistException.class)
    public ResponseEntity<ErrorResponse> handleInformationExist(InformationExistException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.CONFLICT, "INFORMATION_ALREADY_EXISTS", ex.getMessage(), request);
    }

    /**
     * handles invalid format input.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 400 response with a structured error body.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormat(HttpMessageNotReadableException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_FORMAT", "Invalid request body.", request);
    }

    /**
     * handles invalid request data.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 400 response with a structured error body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMethodArgument(MethodArgumentNotValidException ex, HttpServletRequest request){
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        List<String> messages = new ArrayList<>();
        for (FieldError fieldError: errors){
            messages.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        String joinedResponse = String.join("; ", messages);
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", joinedResponse, request);
    }

    /**
     * handles internal server error exception
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 500 response with an internal server error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> internalServerError(Exception ex, HttpServletRequest request){
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR","An unexpected error occurred.", request);
    }

    /**
     * Handles invalid verification code.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 400 response with error details.
     */
    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCodeException(InvalidCodeException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_CODE", ex.getMessage(), request);
    }


    /**
     * Builds an error response.
     *
     * @param status the HTTP status to return.
     * @param error  the error that caused the exception.
     * @param message an explanation of the error.
     * @param request the request that failed.
     * @return the response includes the error body and the status.
     */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), status.value(), error, message, request.getRequestURI());
        return ResponseEntity.status(status).body(errorResponse);
    }
}
