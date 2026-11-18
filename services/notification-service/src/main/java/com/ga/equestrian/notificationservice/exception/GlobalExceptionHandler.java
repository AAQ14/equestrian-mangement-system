package com.ga.equestrian.notificationservice.exception;

import com.ga.equestrian.notificationservice.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

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
     *
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
