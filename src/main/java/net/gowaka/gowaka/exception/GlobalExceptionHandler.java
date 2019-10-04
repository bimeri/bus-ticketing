package net.gowaka.gowaka.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static net.gowaka.gowaka.exception.ErrorCodes.*;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 2:57 AM <br/>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {ResourceNotFoundException.class}) // 404
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(RESOURCE_NOT_FOUND.toString());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(value = {BusinessValidationException.class}) // 422
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(BusinessValidationException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(VALIDATION_ERROR.toString());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    @ExceptionHandler(value = {AccessDeniedException.class}) //401
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(ACCESS_DENIED.toString());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(value = {AuthorizationException.class}) //403
    public ResponseEntity<ErrorResponse> handleAuthorizationException(BadCredentialsException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(ACCESS_FORBIDDEN.toString());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {ApiException.class}) //422
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(ex.getErrorCode());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                                         HttpServletRequest request){
        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse();
        validationErrorResponse.setEndpoint(request.getRequestURI());
        validationErrorResponse.setCode(VALIDATION_ERROR.toString());
        validationErrorResponse.setMessage("MethodArgumentNotValidException");
        List<FieldError> fieldErrorList = ex.getBindingResult().getFieldErrors();

        for(FieldError fieldError: fieldErrorList){
            validationErrorResponse.getErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(validationErrorResponse, HttpStatus.BAD_REQUEST);
    }
}
