package net.gogroups.gowaka.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gogroups.cfs.model.CustomerDTO;
import net.gogroups.cfs.model.SurveyDTO;
import net.gogroups.cfs.service.CfsClientService;
import net.gogroups.dto.PaginatedResponse;
import net.gogroups.gowaka.constant.RefundStatus;
import net.gogroups.gowaka.constant.notification.EmailFields;
import net.gogroups.gowaka.constant.notification.SmsFields;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
import net.gogroups.gowaka.domain.service.utilities.DTFFromDateStr;
import net.gogroups.gowaka.domain.service.utilities.TimeProvider;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.GwCacheLoaderService;
import net.gogroups.gowaka.service.JourneyService;
import net.gogroups.gowaka.service.UserService;
import net.gogroups.notification.model.EmailAddress;
import net.gogroups.notification.model.SendEmailDTO;
import net.gogroups.notification.model.SendSmsDTO;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.payamgo.constants.PayAmGoPaymentStatus;
import net.gogroups.storage.constants.FileAccessType;
import net.gogroups.storage.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nnouka Stephen
 * @date 17 Oct 2019
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JourneyServiceImpl implements JourneyService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TransitAndStopRepository transitAndStopRepository;
    private final JourneyRepository journeyRepository;
    private final JourneyStopRepository journeyStopRepository;
    private final BookedJourneyRepository bookedJourneyRepository;
    private final GgCfsSurveyTemplateJsonRepository ggCfsSurveyTemplateJsonRepository;
    private final CfsClientService cfsClientService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final EmailContentBuilder emailContentBuilder;
    private final GwCacheLoaderService gwCacheLoaderService;

    private static final ZoneId zoneId = ZoneId.of("GMT");

    @Value("${notification.email-from-address)")
    private String fromEmail = "no-reply@mygowaka.com";


    @Override
    public JourneyResponseDTO addJourney(JourneyDTO journey, Long carId) {

        JourneyResponseDTO journeyResponseDTO = mapSaveAndGetJourneyResponseDTO(journey, new Journey(), getOfficialAgencyCarById(carId));
        gwCacheLoaderService.addUpdateJourney(journeyResponseDTO);
        return journeyResponseDTO;
    }

    @Override
    public JourneyResponseDTO updateJourney(JourneyDTO dto, Long journeyId, Long carId) {
        Journey journey = getJourney(journeyId);
        journeyTerminationFilter(journey);
        checkJourneyCarInOfficialAgency(journey);
        JourneyResponseDTO journeyResponseDTO = mapSaveAndGetJourneyResponseDTO(dto, journey, getOfficialAgencyCarById(carId));
        gwCacheLoaderService.addUpdateJourney(journeyResponseDTO);
        return journeyResponseDTO;
    }

    @Override
    public List<JourneyResponseDTO> getAllOfficialAgencyJourneys() {
        OfficialAgency officialAgency = getOfficialAgency(verifyCurrentAuthUser());
        return journeyRepository.findAllByOrderByCreatedAtDescArrivalIndicatorAsc().stream()
                .filter(journey -> {
                    Car car = journey.getCar();
                    if (car == null) return false;
                    if (car instanceof Bus) {
                        if (((Bus) car).getOfficialAgency() == null) return false;
                        return ((Bus) car).getOfficialAgency().equals(officialAgency);
                    }
                    return false;
                }).map(this::mapToJourneyResponseDTO).collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<JourneyResponseDTO> getOfficialAgencyJourneys(Integer pageNumber, Integer limit, Long branchId) {

        Pageable paging = PageRequest.of(pageNumber < 1 ? 0 : pageNumber - 1, limit);

        User user = verifyCurrentAuthUser();
        boolean found = false;
        for (AgencyBranch branch : user.getOfficialAgency().getAgencyBranch()) {
            if (branch.getId().equals(branchId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new ResourceNotFoundException("Branch not found in user's agency");
        }
        Page<Journey> journeyPage = journeyRepository.findByAgencyBranch_IdOrderByCreatedAtDescArrivalIndicatorAsc(branchId, paging);

        List<JourneyResponseDTO> journeys = journeyPage.stream()
                .map(journey -> mapToJourneyResponseDTO(journey, user)).collect(Collectors.toList());

        return PaginatedResponse.<JourneyResponseDTO>builder()
                .items(journeys)
                .count(journeys.size())
                .total((int) journeyPage.getTotalElements())
                .totalPages(journeyPage.getTotalPages())
                .limit(limit)
                .offset((int) paging.getOffset())
                .pageNumber(pageNumber)
                .build();
    }

    @Override
    public JourneyResponseDTO getJourneyById(Long journeyId) {
        Journey journey = getJourney(journeyId);
        List<Bus> buses = getOfficialAgency(verifyCurrentAuthUser())
                .getBuses().stream()
                .filter(bus1 -> journey.getCar().equals(bus1)).collect(Collectors.toList());
        if (buses.isEmpty()) {
            throw new ApiException("Journey\'s car not in AuthUser\'s Agency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return mapToJourneyResponseDTO(journey);
    }

    @Override
    public JourneyResponseDTO getAJourneyById(Long journeyId) {
        Journey journey = getJourney(journeyId);
        return mapToJourneyResponseDTO(journey);
    }

    @Override
    public void addStop(Long journeyId, AddStopDTO addStopDTO) {
        Journey journey = getJourney(journeyId);
        if (journeyTerminationFilter(journey)) {
            checkJourneyCarInOfficialAgency(journey);
            List<JourneyStop> journeyStops = journey.getJourneyStops();
            List<TransitAndStop> transitAndStops = journeyStops.stream().map(
                    JourneyStop::getTransitAndStop
            ).collect(Collectors.toList());
            // save only if transit and stop does not already exist
            if (transitAndStops.stream()
                    .noneMatch(transitAndStop -> transitAndStop.getId()
                            .equals(addStopDTO.getTransitAndStopId()))) {
                JourneyStop journeyStop = new JourneyStop();
                journeyStop.setAmount(addStopDTO.getAmount());
                journeyStop.setTransitAndStop(getTransitAndStop(addStopDTO.getTransitAndStopId()));
                journeyStop.setJourney(journey);
                journeyStops.add(journeyStop);
                journey.setJourneyStops(journeyStops);
                journeyRepository.save(journey);
            }
        }
    }

    @Override
    public void deleteNonBookedJourney(Long journeyId) {
        Journey journey = getJourney(journeyId);
        if (journeyTerminationFilter(journey)) {
            checkJourneyCarInOfficialAgency(journey);
            if (isJourneyNotBooked(journey)) {
                journeyRepository.delete(journey);
                gwCacheLoaderService.deleteJourneyJourney(journey.getAgencyBranch().getOfficialAgency().getId(), journey.getAgencyBranch().getId(), journey.getId());
            }
        }
    }

    @Override
    public void updateJourneyDepartureIndicator(Long journeyId, JourneyDepartureIndicatorDTO journeyDepartureIndicator) {

        Journey journey = getJourney(journeyId);
        if (journeyTerminationFilter(journey)) {
            checkJourneyCarInOfficialAgency(journey);
            journey.setDepartureIndicator(journeyDepartureIndicator.getDepartureIndicator());
            journey = journeyRepository.save(journey);
        }
        if (journeyDepartureIndicator.getDepartureIndicator()) {
            try {
                sendSMSAndEmailNotificationToSubscribers(journey, "just started");
                gwCacheLoaderService.deleteJourneyJourney(journey.getAgencyBranch().getOfficialAgency().getId(), journey.getAgencyBranch().getId(), journey.getId());
            } catch (Exception e) {
                log.error("Error sending request to SMS notifications for journeyId: {} ", journey.getId());
                e.printStackTrace();
            }
        } else {
            JourneyResponseDTO journeyResponseDTO = mapToJourneyResponseDTO(journey, null);
            gwCacheLoaderService.addUpdateJourney(journeyResponseDTO);
        }
    }

    @Override
    public void updateJourneyArrivalIndicator(Long journeyId, JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO) {

        Journey journey = getJourney(journeyId);
        if (journeyDepartureFilter(journey)) {
            checkJourneyCarInOfficialAgency(journey);
            journey.setArrivalIndicator(journeyArrivalIndicatorDTO.getArrivalIndicator());
            journeyRepository.save(journey);
        }
        if (journeyArrivalIndicatorDTO.getArrivalIndicator()) {
            try {
                List<CustomerDTO> customers = new ArrayList<>();
                journey.getBookedJourneys().stream()
                        .filter(bookedJourney -> bookedJourney.getPaymentTransaction().getTransactionStatus().equals(PayAmGoPaymentStatus.COMPLETED.toString()))
                        .map(BookedJourney::getPassengers)
                        .forEach(passengers -> passengers.forEach(passenger -> customers.add(new CustomerDTO(passenger.getEmail(), passenger.getName()))));

                GgCfsSurveyTemplateJson ggCfsSurveyTemplateJson = ggCfsSurveyTemplateJsonRepository.findById("1").get();
                SurveyDTO surveyDTO = new ObjectMapper().readValue(ggCfsSurveyTemplateJson.getSurveyTemplateJson(), SurveyDTO.class);
                surveyDTO.setName("GoWaka Journey - " + journey.getCar().getName() + " from " + journey.getDepartureLocation().getLocation().getAddress());
                cfsClientService.createAndAddCustomerToSurvey(surveyDTO, customers);
            } catch (Exception e) {
                log.error("Error sending request to CFS ");
                e.printStackTrace();
            }
            try {
                sendSMSAndEmailNotificationToSubscribers(journey, "just ended");
            } catch (Exception e) {
                log.error("Error sending request End journey notification sms for JourneyId: {}", journey.getId());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeNonBookedStop(Long journeyId, Long stopId) {
        Journey journey = getJourney(journeyId);
        journeyTerminationFilter(journey);
        checkJourneyCarInOfficialAgency(journey);
        TransitAndStop transitAndStop = getTransitAndStop(stopId);
        if (isStopNotBooked(journey, transitAndStop)) {
            List<JourneyStop> journeyStops = journey.getJourneyStops();
            log.info("removing all previous journeyStops");
            if (journey.getId() != null)
                journeyStopRepository.deleteAllByJourneyId(journey.getId());
            log.info("setting new journeyStops");
            journey.setJourneyStops(journeyStops.stream().filter(
                    j -> j.getTransitAndStop() != null && !j.getTransitAndStop().equals(transitAndStop)
            ).collect(Collectors.toList()));
            journeyRepository.save(journey);
        } else {
            throw new ApiException(ErrorCodes.TRANSIT_AND_STOP_ALREADY_BOOKED.getMessage(),
                    ErrorCodes.TRANSIT_AND_STOP_ALREADY_BOOKED.toString(), HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public JourneyResponseDTO addSharedJourney(JourneyDTO journeyDTO, Long carId) {
        return mapSaveAndGetJourneyResponseDTO(journeyDTO, new Journey(), getPersonalAgencyCarById(carId));
    }

    @Override
    public JourneyResponseDTO updateSharedJourney(JourneyDTO journeyDTO, Long journeyId, Long carId) {
        Journey journey = getJourney(journeyId);
        journeyTerminationFilter(journey);
        checkJourneyCarInPersonalAgency(journey);
        return mapSaveAndGetJourneyResponseDTO(journeyDTO, journey, getPersonalAgencyCarById(carId));
    }

    @Override
    public JourneyResponseDTO getSharedJourneyById(Long journeyId) {
        Journey journey = getJourney(journeyId);
        List<SharedRide> sharedRides = getPersonalAgency(verifyCurrentAuthUser()).getSharedRides()
                .stream()
                .filter(sharedRide -> journey.getCar().getId().equals(sharedRide.getId()))
                .collect(Collectors.toList());
        if (sharedRides.isEmpty()) {
            throw new ApiException("Journey\'s Car not in AuthUser\'s PersonalAgency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return mapToJourneyResponseDTO(journey);
    }

    @Override
    public List<JourneyResponseDTO> getAllPersonalAgencyJourneys() {
        PersonalAgency personalAgency = getPersonalAgency(verifyCurrentAuthUser());
        return journeyRepository.findAllByOrderByCreatedAtDescArrivalIndicatorAsc()
                .stream().filter(
                        journey -> {
                            Car car = journey.getCar();
                            if (car == null) return false;
                            if (car instanceof SharedRide) {
                                if (((SharedRide) car).getPersonalAgency() == null) return false;
                                return ((SharedRide) car).getPersonalAgency().equals(personalAgency);
                            }
                            return false;
                        }
                ).map(this::mapToJourneyResponseDTO).collect(Collectors.toList());

    }

    @Override
    public void updateSharedJourneyDepartureIndicator(Long journeyId, JourneyDepartureIndicatorDTO journeyDepartureIndicator) {
        Journey journey = getJourney(journeyId);
        if (journeyTerminationFilter(journey)) {
            checkJourneyCarInPersonalAgency(journey);
            journey.setDepartureIndicator(journeyDepartureIndicator.getDepartureIndicator());
            journey = journeyRepository.save(journey);
            log.info("Departure Indicator Updated to: {}", journey.getDepartureIndicator());
        }
    }

    @Override
    public void updateSharedJourneyArrivalIndicator(Long journeyId, JourneyArrivalIndicatorDTO journeyArrivalIndicator) {
        Journey journey = getJourney(journeyId);
        log.info("Arrival Indicator: {}", journey.getArrivalIndicator());
        if (journeyDepartureFilter(journey)) {
            checkJourneyCarInPersonalAgency(journey);
            journey.setArrivalIndicator(journeyArrivalIndicator.getArrivalIndicator());
            journey = journeyRepository.save(journey);
            log.info("Arrival Indicator Updated to: {}", journey.getArrivalIndicator());
        }
    }

    @Override
    public void deleteNonBookedSharedJourney(Long journeyId) {
        Journey journey = getJourney(journeyId);
        if (journeyTerminationFilter(journey)) {
            checkJourneyCarInPersonalAgency(journey);
            if (isJourneyNotBooked(journey)) journeyRepository.delete(journey);
        }
    }

    @Override
    public void addStopToPersonalAgency(Long journeyId, AddStopDTO addStopDTO) {
        Journey journey = getJourney(journeyId);
        if (journeyTerminationFilter(journey)) {
            checkJourneyCarInPersonalAgency(journey);
            List<JourneyStop> journeyStops = journey.getJourneyStops();
            List<TransitAndStop> transitAndStops = journeyStops.stream().map(
                    JourneyStop::getTransitAndStop).collect(Collectors.toList());
            if (transitAndStops.stream()
                    .noneMatch(transitAndStop -> transitAndStop.getId()
                            .equals(addStopDTO.getTransitAndStopId()))) {
                JourneyStop journeyStop = new JourneyStop();
                journeyStop.setAmount(addStopDTO.getAmount());
                journeyStop.setTransitAndStop(getTransitAndStop(addStopDTO.getTransitAndStopId()));
                journeyStop.setJourney(journey);
                journeyStops.add(journeyStop);
                journeyRepository.save(journey);
            }
        }
    }

    @Override
    public List<JourneyResponseDTO> searchJourney(Long departureLocationId, Long destinationLocationId, String time) {
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(time, DTFFromDateStr.getDateTimeFormatterFromString(time));
        } catch (Exception ex) {
            //Validation Error
            throw new ApiException("Invalid date time format. Try yyyy-MM-dd HH:mm",
                    ErrorCodes.INVALID_FORMAT.toString(), HttpStatus.BAD_REQUEST);
        }

        return getJourneyResponseDTOS(departureLocationId, destinationLocationId, dateTime);
    }

    @Override
    public List<JourneyResponseDTO> searchJourney() {

        LocalDateTime today = LocalDate.now().atStartOfDay();
        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        Optional<BookedJourney> bookedJourneyOptional = bookedJourneyRepository.findTopByUser_UserIdOrderByIdDesc(currentAuthUser.getId());
        Long departureLocationId = null;
        Long destinationLocationId = null;
        List<JourneyResponseDTO> journeys = new ArrayList<>();
        if (bookedJourneyOptional.isPresent()) {
            BookedJourney bookedJourney = bookedJourneyOptional.get();
            departureLocationId = bookedJourney.getJourney().getDepartureLocation().getId();
            destinationLocationId = bookedJourney.getDestination().getId();
            //swap trip departure and destination to suggest return trip
            journeys.addAll(getJourneyResponseDTOS(destinationLocationId, departureLocationId, today));
        }
        if (journeys.size() < 10 && departureLocationId != null && destinationLocationId != null) {
            journeys.addAll(getJourneyResponseDTOS(departureLocationId, destinationLocationId, today));
        }
        return journeys;
    }

    @Override
    public List<JourneyResponseDTO> searchAllAvailableJourney() {
        return journeyRepository.findAllByDepartureIndicatorFalseOrderByDepartureTimeAsc().stream()
                .map(this::mapToJourneyResponseDTO)
                .collect(Collectors.toList());
    }

    private List<JourneyResponseDTO> getJourneyResponseDTOS(Long departureLocationId, Long destinationLocationId, LocalDateTime dateTime) {

        TransitAndStop departureLocation = getTransitAndStopCanAppendErrMsg(departureLocationId, "Departure");
        TransitAndStop destinationLocation = getTransitAndStopCanAppendErrMsg(destinationLocationId, "Destination");

        List<Journey> journeyList = journeyRepository.findAllByDepartureIndicatorFalseOrderByDepartureTimeAsc();
        return journeyList.stream().filter(
                journey -> journeySearchFilter(journey, departureLocation, destinationLocation, dateTime)
        ).map(this::mapToJourneyResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * verify and return the current user in cases where user id is relevant
     *
     * @return user
     */
    private User verifyCurrentAuthUser() {
        UserDTO authUser = userService.getCurrentAuthUser();
        // get user entity
        Optional<User> optionalUser = userRepository.findById(authUser.getId());
        if (!optionalUser.isPresent()) {
            throw new ApiException("User not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }

    /**
     * Get the personal agency of user or throw api exception if agency is not found
     *
     * @param user
     * @return officialAgency
     */
    private OfficialAgency getOfficialAgency(User user) {
        OfficialAgency officialAgency = user.getOfficialAgency();
        if (officialAgency == null) {
            throw new ApiException("No Official Agency found for this user", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return officialAgency;
    }

    private PersonalAgency getPersonalAgency(User user) {
        PersonalAgency personalAgency = user.getPersonalAgency();
        if (personalAgency == null) {
            throw new ApiException("No Personal Agency found for this user", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return personalAgency;
    }

    /**
     * Gets car in user's official agency. if car not found, throw car not found api exception
     *
     * @param carId
     * @return Car
     */
    private Car getOfficialAgencyCarById(Long carId) {
        List<Car> cars = getOfficialAgency(verifyCurrentAuthUser()).getBuses()
                .stream().filter(bus -> bus.getId().equals(carId)).collect(Collectors.toList());
        if (cars.isEmpty()) {
            throw new ApiException("Car not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return cars.get(0);
    }

    private Car getPersonalAgencyCarById(Long carId) {
        List<Car> cars = getPersonalAgency(verifyCurrentAuthUser()).getSharedRides()
                .stream().filter(sharedRide -> sharedRide.getId().equals(carId))
                .collect(Collectors.toList());
        if (cars.isEmpty()) {
            throw new ApiException("Journey\'s Car not in AuthUser\'s PersonalAgency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return cars.get(0);
    }

    /**
     * throws exception if transitAndStop is not found
     *
     * @param id
     * @return transitAndStop
     */
    private TransitAndStop getTransitAndStop(Long id) {
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findById(id);
        if (!optionalTransitAndStop.isPresent()) {
            throw new ApiException("TransitAndStop not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalTransitAndStop.get();
    }

    /**
     * Throws exception if transitAndStop is not found but appends message to indicate the category
     * of the transitAndStop
     *
     * @param id
     * @param errMsg
     * @return transitAndStop
     */
    private TransitAndStop getTransitAndStopCanAppendErrMsg(Long id, String errMsg) {
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findById(id);
        if (!optionalTransitAndStop.isPresent()) {
            throw new ApiException(errMsg + " TransitAndStop not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalTransitAndStop.get();
    }

    private JourneyResponseDTO mapSaveAndGetJourneyResponseDTO(JourneyDTO journeyDTO, Journey journey, Car car) {

        User user = verifyCurrentAuthUser();
        // Note previous car in journey context
        Car prevCar = journey.getCar();
        journey.setCar(car);
        journey.setAgencyBranch(user.getAgencyBranch());

        TransitAndStop destinationTransitAndStop = getTransitAndStopCanAppendErrMsg(
                journeyDTO.getDestination() == null ? null : journeyDTO.getDestination().getTransitAndStopId()
                , "Destination");
        journey.setDestination(destinationTransitAndStop);
        TransitAndStop departureTransitAndStop = getTransitAndStopCanAppendErrMsg(journeyDTO.getDepartureLocation(), "Departure");
        journey.setDepartureLocation(departureTransitAndStop);
        List<JourneyStop> journeyStops = new ArrayList<>();
        for (AddStopDTO addStopDTO : journeyDTO.getTransitAndStops()) {
            JourneyStop journeyStop1 = new JourneyStop();
            journeyStop1.setTransitAndStop(getTransitAndStop(addStopDTO.getTransitAndStopId()));
            journeyStop1.setAmount(addStopDTO.getAmount());
            journeyStop1.setJourney(journey);
            journeyStops.add(journeyStop1);
        }
        log.info("removing all previous journeyStops");
        if (journey.getId() != null)
            journeyStopRepository.deleteAllByJourneyId(journey.getId());
        log.info("setting new journeyStops");
        journey.setJourneyStops(journeyStops);
        journey.setAmount(journeyDTO.getDestination().getAmount());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journey.setCreatedAt(TimeProvider.now());

        Driver driver = new Driver();
        if (journeyDTO.getDriver() != null) {
            driver.setDriverLicenseNumber(journeyDTO.getDriver().getDriverLicenseNumber());
            driver.setDriverName(journeyDTO.getDriver().getDriverName());
        }
        journey.setDriver(driver);

        journey.setEstimatedArrivalTime(journeyDTO.getEstimatedArrivalTime() == null ? null :
                journeyDTO.getEstimatedArrivalTime().toInstant().atZone(zoneId).toLocalDateTime());
        journey.setDepartureTime(journeyDTO.getDepartureTime() == null ? null :
                journeyDTO.getDepartureTime().toInstant().atZone(zoneId).toLocalDateTime());

        journey = journeyRepository.save(journey);

        // check if this car's seat structure is different from previous car seat structure
        // then send notification where necessary
        // note that null condition is already taken care of by instanceof
        if (prevCar instanceof Bus && car instanceof Bus) {
            Integer prevSeatsNum = ((Bus) prevCar).getNumberOfSeats();
            Integer currSeatsNum = ((Bus) car).getNumberOfSeats();
            if (!prevSeatsNum.equals(currSeatsNum)) {
                try {
                    sendSMSAndEmailNotificationToSubscribers(journey,
                            "changed seat structure: from "
                                    + prevSeatsNum + " to " + currSeatsNum + " seats");
                } catch (Exception e) {
                    log.error("Error sending request to SMS notifications for journeyId: {} ", journey.getId());
                    e.printStackTrace();
                }
            }
        }

        JourneyResponseDTO journeyResponseDTO = new JourneyResponseDTO();
        journeyResponseDTO.setArrivalIndicator(journey.getArrivalIndicator());
        journeyResponseDTO.setCar(getCarResponseDTO(journey.getCar()));
        journeyResponseDTO.setDepartureIndicator(journey.getDepartureIndicator());
        journeyResponseDTO.setDepartureLocation(getLocationResponseDTO(departureTransitAndStop));
        journeyResponseDTO.setDepartureTime(journeyDTO.getDepartureTime());
        journeyResponseDTO.setDestination(getLocationStopResponseDTO(destinationTransitAndStop,
                journeyDTO.getDestination().getAmount()));
        journeyResponseDTO.setDriver(getDriverDTO(journey.getDriver()));
        journeyResponseDTO.setEstimatedArrivalTime(journeyDTO.getEstimatedArrivalTime());
        journeyResponseDTO.setTransitAndStops(
                journey.getJourneyStops().stream().map(
                        journeyStop -> getLocationStopResponseDTO(
                                journeyStop.getTransitAndStop(), journeyStop.getAmount()
                        )
                ).collect(Collectors.toList())
        );
        journeyResponseDTO.setTimestamp(journey.getCreatedAt() == null ? null :
                Date.from(journey.getCreatedAt().atZone(zoneId).toInstant()));

        journeyResponseDTO.setId(journey.getId());
        journeyResponseDTO.setAmount(journey.getAmount());
        if (journey.getAgencyBranch() != null) {
            journeyResponseDTO.setBranchId(journey.getAgencyBranch().getId());
            journeyResponseDTO.setBranchName(journey.getAgencyBranch().getName());
        }
        return journeyResponseDTO;
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

    /**
     * get a response dto with the location and amount attached
     *
     * @param transitAndStop
     * @param amount
     * @return
     */
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

    /**
     * gets the journey by journey id, throws api exception if journey not found
     *
     * @param id
     * @return Journey
     */
    private Journey getJourney(Long id) {
        Optional<Journey> journeyOptional = journeyRepository.findById(id);
        if (!journeyOptional.isPresent()) {
            throw new ApiException("Journey not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return journeyOptional.get();
    }

    private CarResponseDTO getCarResponseDTO(Car car) {
        CarResponseDTO carDTO = new CarResponseDTO();
        if (car != null) {
            carDTO.setId(car.getId());
            carDTO.setName(car.getName());
            carDTO.setLicensePlateNumber(car.getLicensePlateNumber());
            carDTO.setIsCarApproved(car.getIsCarApproved() == null ? false : car.getIsCarApproved());
            carDTO.setIsOfficialAgencyIndicator(car.getIsOfficialAgencyIndicator() == null ? false : car.getIsOfficialAgencyIndicator());
            if (car instanceof Bus) {
                Bus bus = (Bus) car;
                OfficialAgency officialAgency = bus.getOfficialAgency();
                if (officialAgency != null) {
                    carDTO.setAgencyName(officialAgency.getAgencyName());
                    carDTO.setAgencyLogo(fileStorageService.getFilePath(officialAgency.getLogo(), "", FileAccessType.PROTECTED));
                    carDTO.setPolicy(officialAgency.getPolicy());
                    carDTO.setAgencyId(officialAgency.getId());
                }
                carDTO.setNumberOfSeat(bus.getNumberOfSeats());
            } else if (car instanceof SharedRide) {
                PersonalAgency personalAgency = ((SharedRide) car).getPersonalAgency();
                if (personalAgency != null) carDTO.setAgencyName(personalAgency.getName());
            }
            carDTO.setTimestamp(car.getCreatedAt() == null ? null :
                    Date.from(car.getCreatedAt().atZone(zoneId).toInstant()));
        }
        return carDTO;
    }

    private JourneyResponseDTO mapToJourneyResponseDTO(Journey journey) {
        return mapToJourneyResponseDTO(journey, null);
    }

    /**
     * Maps journey to JourneyResponseDTO
     *
     * @param journey
     * @return JourneyResponseDTO
     */
    private JourneyResponseDTO mapToJourneyResponseDTO(Journey journey, User user) {
        JourneyResponseDTO journeyResponseDTO = new JourneyResponseDTO();
        journeyResponseDTO.setArrivalIndicator(journey.getArrivalIndicator());
        journeyResponseDTO.setCar(getCarResponseDTO(journey.getCar()));
        journeyResponseDTO.setDepartureIndicator(journey.getDepartureIndicator());
        if (user != null) {
            journeyResponseDTO.setUserBranch(journey.getAgencyBranch().getId().equals(user.getAgencyBranch().getId()));
        }
        try {
            // these can throw exceptions
            // no need to throw an exception during get, just log the exception in the catch block
            // and show the user the valid responses
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
        if (journey.getAgencyBranch() != null) {
            journeyResponseDTO.setBranchId(journey.getAgencyBranch().getId());
            journeyResponseDTO.setBranchName(journey.getAgencyBranch().getName());
        }
        return journeyResponseDTO;
    }

    /**
     * throw exception of journey is terminated
     *
     * @param journey
     * @return boolean
     */
    private boolean journeyTerminationFilter(Journey journey) {
        if (journey != null && journey.getArrivalIndicator() != null && journey.getArrivalIndicator()) {
            throw new ApiException("Journey already terminated", ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString(), HttpStatus.CONFLICT);
        }
        return true;
    }

    /**
     * throw exception if journey branch is not in user official agency branch
     * update to used branch on Mar14, 2021
     *
     * @param journey
     */
    public void checkJourneyCarInOfficialAgency(Journey journey) {
        User user = verifyCurrentAuthUser();
        if (!journey.getAgencyBranch().getId().equals(user.getAgencyBranch().getId())) {
            throw new ApiException("Journey\'s car not in AuthUser\'s Agency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * throw exception of journey is not started
     *
     * @param journey
     * @return boolean
     */
    private boolean journeyDepartureFilter(Journey journey) {
        if (!journey.getDepartureIndicator()) {
            throw new ApiException("Journey not started", ErrorCodes.JOURNEY_NOT_STARTED.toString(), HttpStatus.CONFLICT);
        }
        return true;
    }

    /**
     * throw exception if journey car is not in official agency
     *
     * @param journey
     */
    private void checkJourneyCarInPersonalAgency(Journey journey) {
        List<Car> cars = getPersonalAgency(verifyCurrentAuthUser())
                .getSharedRides().stream()
                .filter(sharedRide -> journey.getCar() != null && journey.getCar().getId().equals(sharedRide.getId()))
                .collect(Collectors.toList());
        if (cars.isEmpty()) {
            throw new ApiException("Journey\'s Car not in AuthUser\'s PersonalAgency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Returns true if journey has been booked, false otherwise
     *
     * @param journey
     * @return boolean
     */
    private boolean isJourneyNotBooked(Journey journey) {
        List<BookedJourney> bookedJourneys = journey.getBookedJourneys();
        if (!bookedJourneys.isEmpty()) {
            throw new ApiException("Bookings Exist for this Journey, cannot delete", ErrorCodes.OPERATION_NOT_ALLOWED.toString(), HttpStatus.METHOD_NOT_ALLOWED);
        }
        return true;
    }

    /**
     * filter for journey search includes destination location, departure location and journey stops
     *
     * @param journey
     * @param departureLocation
     * @param destinationLocation
     * @param dateTime
     * @return
     */
    private boolean journeySearchFilter(Journey journey, TransitAndStop departureLocation,
                                        TransitAndStop destinationLocation, LocalDateTime dateTime) {
        // users destination can either be journey destination or one of the journey stops location
        boolean anyStopMatch = journey.getJourneyStops().stream().anyMatch(
                journeyStop -> journeyStop.getTransitAndStop().getId().equals(destinationLocation.getId())
        );
        return (journey.getDepartureTime().isAfter(dateTime) || journey.getDepartureTime().isEqual(dateTime)) &&
                journey.getDepartureLocation().getId().equals(departureLocation.getId()) &&
                (journey.getDestination().getId().equals(destinationLocation.getId()) || anyStopMatch);
    }

    private boolean isStopNotBooked(Journey journey, TransitAndStop transitAndStop) {
        /*
         * for a stop to be booked in a particular journey
         * the stop should have booked journeys and
         * that particular journey should be one of them
         * since a stop can have booked journeys, and still be not booked for this particular journey
         *
         * an additional check is also to ensure that even if a booked journey exists for this stop
         * and is equal to this particular journey in question,
         * the stop should be the same as the destination of the booked journey
         *
         * this additional check can be gracefully ignored
         * since the mapping between transitAndStops and bookedJourneys ensures that
         * if a transitAndStop has a bookedJourney, then that transitAndStop should
         * be the destination of the bookedJourney
         */
        return transitAndStop == null || transitAndStop.getBookedJourneys() == null || transitAndStop.getBookedJourneys().stream().noneMatch(
                bookedJourney -> bookedJourney != null && bookedJourney.getJourney() != null && bookedJourney.getJourney().equals(journey)
                /*&& bookedJourney.getDestination() != null && bookedJourney.getDestination().equals(transitAndStop)*/
        );
    }

    private void sendSMSAndEmailNotificationToSubscribers(Journey journey, String appendMessage) {

        Set<String> passengersEmails = new HashSet<>();
        journey.getBookedJourneys().stream()
                .filter(bookedJourney -> bookedJourney.getPaymentTransaction().getTransactionStatus().equalsIgnoreCase(PayAmGoPaymentStatus.COMPLETED.name()))
                .filter(bookedJourney -> {
                    RefundPaymentTransaction refundPaymentTransaction = bookedJourney.getPaymentTransaction().getRefundPaymentTransaction();
                    if (refundPaymentTransaction == null) {
                        return true;
                    } else {
                        return refundPaymentTransaction.getRefundStatus().equalsIgnoreCase(RefundStatus.PENDING.name());
                    }
                }).forEach(bookedJourney -> {
            Set<String> passengersPhoneNumbers = new HashSet<>();
            bookedJourney.getPassengers().forEach(passenger -> {
                if (bookedJourney.getSmsNotification()) {
                    passengersPhoneNumbers.add(passenger.getPhoneNumber());
                }
                if (passenger.getEmail() != null) {
                    passengersEmails.add(passenger.getEmail());
                }
            });

            String message = "Your trip to " + bookedJourney.getDestination().getLocation().getCity() + " on GoWaka " + appendMessage;
            passengersPhoneNumbers.forEach(phone -> {
                SendSmsDTO sendSmsDTO = new SendSmsDTO();
                sendSmsDTO.setMessage(message);
                sendSmsDTO.setPhoneNumber(phone);
                sendSmsDTO.setSenderLabel(SmsFields.SMS_LABEL.getMessage());
                sendSmsDTO.setProcessingNumber(UUID.randomUUID().toString());
                notificationService.sendSMS(sendSmsDTO);
            });
        });

        String message = emailContentBuilder.buildJourneyStatusEmail("Your GoWaka trip departing from " + journey.getDepartureLocation().getLocation().getCity() + " " + appendMessage);

        SendEmailDTO emailDTO = new SendEmailDTO();
        emailDTO.setSubject(EmailFields.JOURNEY_UPDATES.getMessage());
        emailDTO.setMessage(message);
        Set<EmailAddress> emailAddresses = passengersEmails.stream()
                .map(email -> new EmailAddress(email, email))
                .collect(Collectors.toSet());

        emailDTO.setFromAddress(fromEmail);
        emailDTO.setToAddresses(Collections.singletonList(new EmailAddress(fromEmail, fromEmail)));
        emailDTO.setCcAddresses(Collections.emptyList());
        emailDTO.setBccAddresses(new ArrayList<>(emailAddresses));
        notificationService.sendEmail(emailDTO);
    }
}
