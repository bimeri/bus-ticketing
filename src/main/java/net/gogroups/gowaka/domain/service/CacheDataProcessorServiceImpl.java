package net.gogroups.gowaka.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.JourneyRepository;
import net.gogroups.gowaka.domain.repository.PassengerRepository;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.CacheDataProcessorService;
import net.gogroups.storage.constants.FileAccessType;
import net.gogroups.storage.service.FileStorageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static net.gogroups.gowaka.constant.StaticContent.MIM_TIME_TO_WAIT_FOR_PAYMENT;
import static net.gogroups.payamgo.constants.PayAmGoPaymentStatus.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/20/21 5:31 PM <br/>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheDataProcessorServiceImpl implements CacheDataProcessorService {


    private static final ZoneId zoneId = ZoneId.of("GMT");

    private final JourneyRepository journeyRepository;
    private final PassengerRepository passengerRepository;
    private final FileStorageService fileStorageService;

    @Override
    public AllAvailableJourneyAndBookedSeatsDTO getAllAvailableJourneys() {

        AllAvailableJourneyAndBookedSeatsDTO allAvailableJourneyAndBookedSeatsDTO = new AllAvailableJourneyAndBookedSeatsDTO();
        List<Journey> journeys = journeyRepository.findByDepartureIndicatorFalse();
        for (Journey journey : journeys) {
            allAvailableJourneyAndBookedSeatsDTO.getJourneys().add(mapToJourneyResponseDTO(journey));
            allAvailableJourneyAndBookedSeatsDTO.getBookedSeats().add(new AllAvailableJourneyAndBookedSeatsDTO.BookedSeat(journey.getId(), getAllBookedSeats(journey)));
        }
        return allAvailableJourneyAndBookedSeatsDTO;
    }


    private List<Integer> getAllBookedSeats(Journey journey) {

        Set<Integer> seats = new HashSet<>();
        journey.getBookedJourneys().stream()
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction() != null)
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction().getTransactionStatus().equals(INITIATED.toString())
                        || bookedJourney.getPaymentTransaction().getTransactionStatus().equals(WAITING.toString())
                        || bookedJourney.getPaymentTransaction().getTransactionStatus().equals(COMPLETED.toString())
                ).forEach(bookedJourney -> {
            PaymentTransaction paymentTransaction = bookedJourney.getPaymentTransaction();
            String transactionStatus = paymentTransaction.getTransactionStatus();
            long untilMinute = paymentTransaction.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.MINUTES);
            if ((transactionStatus.equals(WAITING.toString()) || transactionStatus.equals(INITIATED.toString()))
                    && untilMinute > MIM_TIME_TO_WAIT_FOR_PAYMENT) {
                List<Passenger> passengers = bookedJourney.getPassengers().stream().map(passenger -> {
                    passenger.setSeatNumber(0);
                    return passenger;
                }).collect(Collectors.toList());
                // update seatNumber if more than waiting limit,
                passengerRepository.saveAll(passengers);
            } else {
                seats.addAll(bookedJourney.getPassengers().stream().map(Passenger::getSeatNumber).collect(Collectors.toList()));
            }
        });
        return new ArrayList<>(seats);
    }

    private JourneyResponseDTO mapToJourneyResponseDTO(Journey journey) {
        JourneyResponseDTO journeyResponseDTO = new JourneyResponseDTO();
        journeyResponseDTO.setArrivalIndicator(journey.getArrivalIndicator());
        journeyResponseDTO.setCar(getCarResponseDTO(journey.getCar(), journey.getAgencyBranch().getOfficialAgency()));
        journeyResponseDTO.setDepartureIndicator(journey.getDepartureIndicator());
        journeyResponseDTO.setUserBranch(false);

        try {
            journeyResponseDTO.setDepartureLocation(getLocationResponseDTO(
                    journey.getDepartureLocation()
            ));
            journeyResponseDTO.setDepartureTime(
                    Date.from(journey.getDepartureTime() == null ? LocalDateTime.now().atZone(zoneId).toInstant() :
                            journey.getDepartureTime().atZone(zoneId).toInstant())
            );
            journeyResponseDTO.setDestination(getLocationStopResponseDTO(
                    journey.getDestination(),
                    journey.getAmount()
            ));
            journeyResponseDTO.setTransitAndStops(
                    journey.getJourneyStops().stream().map(
                            journeyStop -> getLocationStopResponseDTO(
                                    journeyStop.getTransitAndStop(), journeyStop.getAmount()
                            )
                    ).collect(Collectors.toList())
            );
            journeyResponseDTO.setEstimatedArrivalTime(journey.getEstimatedArrivalTime() == null ? null :
                    Date.from(journey.getEstimatedArrivalTime().atZone(zoneId).toInstant()));

            journeyResponseDTO.setTimestamp(journey.getCreatedAt() == null ? null :
                    Date.from(journey.getCreatedAt().atZone(zoneId).toInstant()));
        } catch (Exception e) {
            log.warn("An exception occurred during get: {}", e.getMessage());
        }
        journeyResponseDTO.setDriver(getDriverDTO(journey.getDriver()));
        journeyResponseDTO.setId(journey.getId());
        journeyResponseDTO.setAmount(journey.getAmount());
        journeyResponseDTO.setDepartureTimeDue(LocalDateTime.now().isAfter(journey.getDepartureTime()));
        if(journey.getAgencyBranch() != null) {
            journeyResponseDTO.setBranchId(journey.getAgencyBranch().getId());
            journeyResponseDTO.setBranchName(journey.getAgencyBranch().getName());
        }
        return journeyResponseDTO;
    }

    private CarResponseDTO getCarResponseDTO(Car car, OfficialAgency officialAgency) {
        CarResponseDTO carDTO = new CarResponseDTO();
        carDTO.setId(car.getId());
        carDTO.setName(car.getName());
        carDTO.setLicensePlateNumber(car.getLicensePlateNumber());
        carDTO.setIsCarApproved(car.getIsCarApproved() != null && car.getIsCarApproved());
        carDTO.setIsOfficialAgencyIndicator(car.getIsOfficialAgencyIndicator() != null && car.getIsOfficialAgencyIndicator());
        carDTO.setNumberOfSeat(((Bus) car).getNumberOfSeats());
        carDTO.setSeatStructureCode(((Bus) car).getSeatStructure().getSeatStructureCode());
        carDTO.setAgencyName(officialAgency.getAgencyName());
        carDTO.setAgencyLogo(fileStorageService.getFilePath(officialAgency.getLogo(), "", FileAccessType.PROTECTED));
        carDTO.setPolicy(officialAgency.getPolicy());
        carDTO.setAgencyId(officialAgency.getId());
        carDTO.setTimestamp(car.getCreatedAt() == null ? null :
                Date.from(car.getCreatedAt().atZone(zoneId).toInstant()));
        return carDTO;
    }

    private LocationResponseDTO getLocationResponseDTO(TransitAndStop transitAndStop) {
        Location location = transitAndStop.getLocation() != null ? transitAndStop.getLocation() : new Location();
        LocationResponseDTO locationResponseDTO = new LocationResponseDTO();
        locationResponseDTO.setId(transitAndStop.getId());
        locationResponseDTO.setAddress(location.getAddress());
        locationResponseDTO.setCity(location.getCity());
        locationResponseDTO.setState(location.getState());
        locationResponseDTO.setCountry(location.getCountry());
        return locationResponseDTO;
    }

    private LocationStopResponseDTO getLocationStopResponseDTO(TransitAndStop transitAndStop, double amount) {
        Location location = transitAndStop.getLocation() != null ? transitAndStop.getLocation() : new Location();
        LocationStopResponseDTO locationStopResponseDTO = new LocationStopResponseDTO();
        locationStopResponseDTO.setId(transitAndStop.getId());
        locationStopResponseDTO.setAddress(location.getAddress());
        locationStopResponseDTO.setCity(location.getCity());
        locationStopResponseDTO.setCountry(location.getCountry());
        locationStopResponseDTO.setState(location.getState());
        locationStopResponseDTO.setAmount(amount);
        return locationStopResponseDTO;
    }

    private DriverDTO getDriverDTO(Driver driver) {
        DriverDTO driverDTO = new DriverDTO();
        if (driver == null) {
            return null;
        }
        driverDTO.setDriverName(driver.getDriverName());
        driverDTO.setDriverLicenseNumber(driver.getDriverLicenseNumber());
        return driverDTO;
    }

}
