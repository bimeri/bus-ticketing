package net.gowaka.gowaka.exception;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:42 PM <br/>
 */
public enum ErrorCodes {
    RESOURCE_NOT_FOUND("Resource Not Found"),
    ACCESS_DENIED("Access is denied"),
    ACCESS_FORBIDDEN("Access is forbidden"),
    VALIDATION_ERROR("Validation Error"),
    INT_SERVER_ERROR("Internal Server error"),
    EXT_SERVER_ERROR("External server error"),
    EXT_SERVICE_UNAVAILABLE("External service is unavailable"),
    BAD_CREDENTIALS("Bad Credentials"),
    USER_NOT_IN_AGENCY("User not in this agency"),
    USER_ALREADY_IN_AN_AGENCY("User is already in this agency"),
    LICENSE_PLATE_NUMBER_ALREADY_IN_USE("License plate number is already in use."),
    TRANSIT_AND_STOP_ALREADY_IN_USE("Transit and stop is already in use."),
    TRANSIT_AND_STOP_ALREADY_BOOKED("Transit and stop is already booked."),
    INVALID_FORMAT("Invalid Format"),
    JOURNEY_ALREADY_TERMINATED("This Journey is already terminated."),
    JOURNEY_ALREADY_STARTED("This Journey is already started."),
    JOURNEY_NOT_STARTED("This Journey has not started."),
    OPERATION_NOT_ALLOWED("This operation is not allowed."),
    CAR_NOT_IN_USERS_AGENCY("This car is not in user\"s agency"),
    CAR_HAS_JOURNEY("This Car has one or more journeys"),
    CAR_ALREADY_HAS_JOURNEY("This Car has one or more booked journeys"),
    LOCATION_HAS_BOOKED_JOURNEY("This location has one or more booked journeys"),
    SEAT_STRUCTURE_NOT_FOUND("The seat structure does not exist or may have been deleted"),
    MD5_HASH_FAILED("Failed to execute MD5 hashing"),
    FILE_NOT_FOUND("File not found "),
    SEAT_ALREADY_TAKEN("Seat already taken.");

    private String message;

    ErrorCodes(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
