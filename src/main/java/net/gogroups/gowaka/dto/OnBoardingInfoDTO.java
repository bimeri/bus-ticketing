package net.gogroups.gowaka.dto;

import lombok.Getter;
import lombok.Setter;
import net.gogroups.gowaka.domain.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OnBoardingInfoDTO {

    private Double amount;
    private String currencyCode;
    private String carDriverName;
    private String carLicenseNumber;
    private String carName;
    private String agencyName;
    private String departureLocation;
    private LocalDateTime departureTime;
    private String destinationLocation;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String paidBy;

    private List<PassengerDTO> passengers = new ArrayList<>();

    public OnBoardingInfoDTO(BookedJourney bookedJourney) {
        if (bookedJourney != null) {
            PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();
            Journey journey = bookedJourney.getJourney();
            // amount
            this.amount = bookedJourney.getAmount();
            // currency
            if (paymentTransaction != null) {
                this.currencyCode = paymentTransaction.getCurrencyCode();
                this.paymentMethod = paymentTransaction.getPaymentChannel();
                this.paymentDate = paymentTransaction.getPaymentDate();
                this.paidBy = paymentTransaction.getAppUserFirstName() + " " + paymentTransaction.getAppUserLastName();
            }
            // journey
            if (journey != null) {
                Driver driver = journey.getDriver();
                if (driver != null) this.carDriverName = driver.getDriverName();
                Car car = journey.getCar();
                if (car != null) {
                    this.carLicenseNumber = car.getLicensePlateNumber();
                    this.carName = car.getName();
                    if (car.getIsOfficialAgencyIndicator() != null && car.getIsOfficialAgencyIndicator()) {
                        this.agencyName = ((Bus) car).getOfficialAgency().getAgencyName();
                    }
                }
                // location
                TransitAndStop transitAndStopDest = bookedJourney.getDestination();
                TransitAndStop transitAndStopDep = journey.getDepartureLocation();
                if (transitAndStopDest != null) {
                    Location locationDest = transitAndStopDest.getLocation();
                    if (locationDest != null) this.destinationLocation =
                            (locationDest.getAddress() == null ? "" : locationDest.getAddress() + ", ") +
                                    (locationDest.getCity() == null ? "" : locationDest.getCity() + ", ") +
                                    (locationDest.getState() == null ? "" : locationDest.getState() + ", ") +
                                    (locationDest.getCountry() == null ? "" : locationDest.getCountry());
                }
                if (transitAndStopDep != null) {
                    Location locationDep = transitAndStopDep.getLocation();
                    if (locationDep != null) this.departureLocation =
                            (locationDep.getAddress() == null ? "" : locationDep.getAddress() + ", ") +
                                    (locationDep.getCity() == null ? "" : locationDep.getCity() + ", ") +
                                    (locationDep.getState() == null ? "" : locationDep.getState() + ", ") +
                                    (locationDep.getCountry() == null ? "" : locationDep.getCountry());
                }
                // time
                this.departureTime = journey.getDepartureTime();
            }
            // passenger
            bookedJourney.getPassengers().forEach(pge -> {
                PassengerDTO passengerDTO = new PassengerDTO();
                passengerDTO.setPassengerEmail(pge.getEmail());
                passengerDTO.setPassengerName(pge.getName());
                passengerDTO.setPassengerIdNumber(pge.getIdNumber());
                passengerDTO.setPassengerPhoneNumber(pge.getPhoneNumber());
                passengerDTO.setPassengerSeatNumber(pge.getSeatNumber());
                passengerDTO.setCheckedInCode(pge.getCheckedInCode());
                passengerDTO.setCheckedIn(pge.getPassengerCheckedInIndicator());
                passengers.add(passengerDTO);
            });
        }
    }
}
