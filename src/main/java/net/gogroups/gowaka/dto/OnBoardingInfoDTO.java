package net.gogroups.gowaka.dto;

import lombok.Getter;
import lombok.Setter;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.service.utilities.QRCodeProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class OnBoardingInfoDTO {

    private Long id;
    private Double amount;
    private String currencyCode;
    private String carDriverName;
    private String carLicenseNumber;
    private String carName;
    private String agencyName;
    private String agencyAddress;
    private String agencyPhoneNumber;
    private String agencyBranchName;
    private String agencyBranchAddress;
    private String departureLocation;
    private LocalDateTime departureTime;
    private String destinationLocation;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String paidBy;
    private String tlaDestinationLocation;
    private String tlaDepartureLocation;

    private Boolean hasRefund = Boolean.FALSE;
    private String refundStatus = "NO REFUND";
    private Double approvedRefundedAmount = 0.0;
    private String refundApprovedByName;
    private String refundApprovedByEmail;
    private LocalDateTime refundApprovedDate;
    private Boolean isSubscribeToSMSNotification = false;

    private List<PassengerDTO> passengers = new ArrayList<>();
    private Set<String> uniquePhoneNumbers = new HashSet<>();

    public OnBoardingInfoDTO(BookedJourney bookedJourney) {
        if (bookedJourney != null) {
            PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();
            Journey journey = bookedJourney.getJourney();
            isSubscribeToSMSNotification = bookedJourney.getSmsNotification();
            // amount
            this.amount = bookedJourney.getAmount();
            this.id = bookedJourney.getId();
            // currency
            if (paymentTransaction != null) {
                this.currencyCode = paymentTransaction.getCurrencyCode();
                this.paymentMethod = paymentTransaction.getPaymentChannel();
                this.paymentDate = paymentTransaction.getPaymentDate();
                this.paidBy = paymentTransaction.getAppUserFirstName() + " " + paymentTransaction.getAppUserLastName();

                RefundPaymentTransaction refundPaymentTransaction = paymentTransaction.getRefundPaymentTransaction();
                if (refundPaymentTransaction != null) {
                    this.hasRefund = Boolean.TRUE;
                    this.approvedRefundedAmount = refundPaymentTransaction.getAmount();
                    this.refundStatus = refundPaymentTransaction.getRefundStatus();
                    this.refundApprovedByName = refundPaymentTransaction.getApprovalName();
                    this.refundApprovedByEmail = refundPaymentTransaction.getApprovalEmail();
                    this.refundApprovedDate = refundPaymentTransaction.getRespondedDate();
                }
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
                        this.agencyAddress = ((Bus) car).getOfficialAgency().getAddress();
                        this.agencyPhoneNumber = ((Bus) car).getOfficialAgency().getPhoneNumber();
                    }
                }
                // location
                TransitAndStop transitAndStopDest = bookedJourney.getDestination();
                TransitAndStop transitAndStopDep = journey.getDepartureLocation();
                if (transitAndStopDest != null) {
                    Location locationDest = transitAndStopDest.getLocation();
                    if (locationDest != null) {
                        this.destinationLocation =
                                (locationDest.getAddress() == null ? "" : locationDest.getAddress() + ", ") +
                                        (locationDest.getCity() == null ? "" : locationDest.getCity() + ", ") +
                                        (locationDest.getState() == null ? "" : locationDest.getState() + ", ") +
                                        (locationDest.getCountry() == null ? "" : locationDest.getCountry());
                        this.tlaDestinationLocation =
                                (locationDest.getTlaAddress() == null ? "" : locationDest.getTlaAddress() + ", ") +
                                        (locationDest.getTlaCity() == null ? "" : locationDest.getTlaCity() + ", ") +
                                        (locationDest.getState() == null ? "" : locationDest.getTlaState() + ", ") +
                                        (locationDest.getTlaCountry() == null ? "" : locationDest.getTlaCountry());
                    }
                }
                if (transitAndStopDep != null) {
                    Location locationDep = transitAndStopDep.getLocation();
                    if (locationDep != null) {
                        this.departureLocation =
                                (locationDep.getAddress() == null ? "" : locationDep.getAddress() + ", ") +
                                        (locationDep.getCity() == null ? "" : locationDep.getCity() + ", ") +
                                        (locationDep.getState() == null ? "" : locationDep.getState() + ", ") +
                                        (locationDep.getCountry() == null ? "" : locationDep.getCountry());
                        this.tlaDepartureLocation =
                                (locationDep.getTlaAddress() == null ? "" : locationDep.getTlaAddress() + ", ") +
                                        (locationDep.getTlaCity() == null ? "" : locationDep.getTlaCity() + ", ") +
                                        (locationDep.getState() == null ? "" : locationDep.getTlaState() + ", ") +
                                        (locationDep.getTlaCountry() == null ? "" : locationDep.getTlaCountry());
                    }
                }
                // time
                this.departureTime = journey.getDepartureTime();
                this.agencyBranchName = journey.getAgencyBranch().getName();
                this.agencyBranchAddress = journey.getAgencyBranch().getAddress();
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
                passengerDTO.setQRCheckedInImage(QRCodeProvider.getQRCodeBase64EncodedImage(pge.getCheckedInCode()));
                uniquePhoneNumbers.add(pge.getPhoneNumber());
            });
        }
    }
}
