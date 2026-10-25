package com.ga.equestrian.exception;

import com.ga.equestrian.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
        log.warn("Unreadable request body: {}", ex.getMessage());
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
     *
     * Handles uploads that fail validation, such as empty files,
     * a file too large, or a type is not allowed.
     *
     * @param ex   the exception thrown
     * @param request  the request that caused the error
     * @return    a 400 response with error details.
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileException(InvalidFileException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_FILE", ex.getMessage(), request);
    }

    /**
     * Handles bookings that break a business rule.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 422 response with error details.
     */
    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBooking(InvalidBookingException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_BOOKING", ex.getMessage(), request);
    }


    /**
     * Handles users who are not allowed to perform an action.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 403 response with error details.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
    }

    /**
     * Handles request parameters with a value of the wrong type, such as an unknown status.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 400 response with a message naming the invalid parameter.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'.";
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, request);
    }

    /**
     * Handles rejected password changes, such as a wrong current password.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 400 response with error details.
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", ex.getMessage(), request);
    }

    /**
     * Handles actions on users that are not allowed, such as an admin deactivating their own account.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 422 response with error details.
     */
    @ExceptionHandler(InvalidUserActionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserAction(InvalidUserActionException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_USER_ACTION", ex.getMessage(), request);
    }

    /**
     * Handles attempts to buy a seat that is already sold.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 409 response with error details.
     */
    @ExceptionHandler(SeatAlreadySoldException.class)
    public ResponseEntity<ErrorResponse> handleSeatAlreadySold(SeatAlreadySoldException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.CONFLICT, "SEAT_ALREADY_SOLD", ex.getMessage(), request);
    }

    /**
     * Handles attempts to cancel a ticket that is already cancelled.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 409 response with error details.
     */
    @ExceptionHandler(TicketAlreadyCancelledException.class)
    public ResponseEntity<ErrorResponse> handleTicketAlreadyCancelled(TicketAlreadyCancelledException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.CONFLICT, "TICKET_ALREADY_CANCELLED", ex.getMessage(), request);
    }

    /**
     * Handles attempts to cancel a ticket that is not allowed to be cancelled.
     * E.g. the tournament status is anything else upcoming.
     *
     * @param ex the exception thrown.
     * @param request the request that caused the error.
     * @return a 422 response with error details.
     */
    @ExceptionHandler(TicketCancellationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleTicketCancellationNotAllowed(TicketCancellationNotAllowedException ex, HttpServletRequest request){
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "TICKET_CANCELLATION_NOT_ALLOWED", ex.getMessage(), request);
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
