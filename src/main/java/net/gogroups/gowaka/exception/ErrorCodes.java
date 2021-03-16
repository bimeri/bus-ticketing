package net.gogroups.gowaka.exception;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/3/19 5:42 PM <br/>
 */
public enum ErrorCodes {
    RESOURCE_NOT_FOUND("Resource Not Found"),
    ACCESS_DENIED("Access is denied"),
    ACCESS_FORBIDDEN("Access is forbidden"),
    VALIDATION_ERROR("Validation Error"),
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
    PASSENGER_ALREADY_CHECKED_IN("This passenger is already checked in"),
    SEAT_ALREADY_TAKEN("Seat already taken."),
    PAYMENT_NOT_COMPLETED("Payment is not completed."),
    RESOURCE_ALREADY_EXIST("Resource already exist."),
    ALREADY_REFUNDED_REQUEST("Request already refunded"),
    INVALID_AMOUNT_LIMIT("Amount must not be more than ticket fee"),
    BRANCH_HAS_JOURNEY("This branch has assigned journeys"),
    BRANCH_HAS_USERS("This branch has assigned users"),
    REFUND_REQUEST_NOT_APPROVED("Refund request not approved.");

    private String message;

    ErrorCodes(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
