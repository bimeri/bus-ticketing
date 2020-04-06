package net.gowaka.gowaka.dto;

import lombok.Getter;
import lombok.Setter;
import net.gowaka.gowaka.domain.model.*;

import java.time.LocalDateTime;
@Getter
@Setter
public class OnBoardingInfoDTO {
    private Double amount;
    private String currencyCode;
    private String carDriverName;
    private String carLicenseNumber;
    private String carName;
    private String departureLocation;
    private LocalDateTime departureTime;
    private String destinationLocation;
    private String passengerEmail;
    private String passengerIdNumber;
    private String passengerName;
    private String passengerPhoneNumber;
    private Integer passengerSeatNumber;
    private String checkedInCode;
    private Boolean passengerCheckedInIndicator;

    public OnBoardingInfoDTO(BookedJourney bookedJourney) {
        if (bookedJourney != null) {
            PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();
            Journey journey = bookedJourney.getJourney();
            Passenger passenger = bookedJourney.getPassenger();
            // amount
            this.amount = bookedJourney.getAmount();
            // currency
            if (paymentTransaction != null) {
                this.currencyCode = paymentTransaction.getCurrencyCode();
            }
            // journey
            if (journey != null) {
                Driver driver = journey.getDriver();
                if (driver != null) this.carDriverName = driver.getDriverName();
                Car car = journey.getCar();
                if (car != null) {
                    this.carLicenseNumber = car.getLicensePlateNumber();
                    this.carName = car.getName();
                }
                // location
                TransitAndStop transitAndStopDest = journey.getDestination();
                TransitAndStop transitAndStopDep = journey.getDepartureLocation();
                if (transitAndStopDest != null) {
                    Location locationDest = transitAndStopDest.getLocation();
                    if (locationDest != null) this.destinationLocation =
                            (locationDest.getAddress() == null ? "": locationDest.getAddress() + ", ") +
                                    (locationDest.getCity() == null ? "" : locationDest.getCity() + ", ") +
                                    (locationDest.getState() == null ? "" : locationDest.getState() + ", ") +
                                    (locationDest.getCountry() == null ? "" : locationDest.getCountry());
                }
                if (transitAndStopDep != null) {
                    Location locationDep = transitAndStopDep.getLocation();
                    if (locationDep != null) this.departureLocation =
                            (locationDep.getAddress() == null ? "": locationDep.getAddress() + ", ") +
                                    (locationDep.getCity() == null ? "" : locationDep.getCity() + ", ") +
                                    (locationDep.getState() == null ? "" : locationDep.getState() + ", ") +
                                    (locationDep.getCountry() == null ? "" : locationDep.getCountry());
                }
                // time
                this.departureTime = journey.getDepartureTime();
            }
            // passenger
            if (passenger != null) {
                this.passengerName = passenger.getPassengerName();
                this.passengerIdNumber = passenger.getPassengerIdNumber();
                this.passengerSeatNumber = passenger.getSeatNumber();
                this.passengerEmail = passenger.getPassengerEmail();
                this.passengerPhoneNumber = passenger.getPassengerPhoneNumber();
            }
            // checkInCode
            this.checkedInCode = bookedJourney.getCheckedInCode();
            // checkInIndicator
            this.passengerCheckedInIndicator = bookedJourney.getPassengerCheckedInIndicator();
        }
    }
}
