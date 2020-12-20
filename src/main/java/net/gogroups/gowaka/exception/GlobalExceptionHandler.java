package net.gogroups.gowaka.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static net.gogroups.gowaka.exception.ErrorCodes.*;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 2:57 AM <br/>
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {ResourceNotFoundException.class}) // 404
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(RESOURCE_NOT_FOUND.toString());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(value = {net.gogroups.client.ApiException.class}) //422
    public ResponseEntity<ErrorResponse> handleApiException(net.gogroups.client.ApiException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(ex.getErrorCode());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        log.info("Handling Exception class: ApiException, Cause {}", ex.getHttpStatus());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }
    @ExceptionHandler(value = {BusinessValidationException.class}) // 422
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(BusinessValidationException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(VALIDATION_ERROR.toString());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    @ExceptionHandler(value = {ResourceAlreadyExistException.class}) // 422
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistException(ResourceAlreadyExistException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(RESOURCE_ALREADY_EXIST.toString());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
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
        List<FieldError> fieldErrorList = ex.getBindingResult().getFieldErrors();
        StringBuilder message = new StringBuilder("MethodArgumentNotValidException:");

        for(FieldError fieldError: fieldErrorList){
            validationErrorResponse.addError(new ValidationErrorResponse.ErrorItem(fieldError.getField(), fieldError.getDefaultMessage()));
            message.append(" #").append(fieldError.getField());
        }
        message.append(" @errors.");
        validationErrorResponse.setMessage(message.toString());

        return new ResponseEntity<>(validationErrorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                                                                               HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(INVALID_FORMAT.toString());
        String dev_message = ex.getMessage();
        String message = null;
        if (dev_message != null ){
            if (dev_message.contains("expected format") && dev_message.contains(";")){
                message = dev_message.substring(dev_message.indexOf("expected format"), dev_message.indexOf(';'));
            }else {
                message = dev_message;
            }
        }
        errorResponse.setMessage(message);
        errorResponse.setEndpoint(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
