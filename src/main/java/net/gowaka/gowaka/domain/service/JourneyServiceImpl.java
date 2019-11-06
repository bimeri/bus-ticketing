package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.JourneyRepository;
import net.gowaka.gowaka.domain.repository.TransitAndStopRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.domain.service.utilities.TimeProvider;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.service.JourneyService;
import net.gowaka.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nnouka Stephen
 * @date 17 Oct 2019
 */
@Service
public class JourneyServiceImpl implements JourneyService {
    private UserService userService;
    private UserRepository userRepository;
    private TransitAndStopRepository transitAndStopRepository;
    private JourneyRepository journeyRepository;
    private ZoneId zoneId = ZoneId.of("GMT");

    @Autowired
    public JourneyServiceImpl(UserService userService, UserRepository userRepository, TransitAndStopRepository transitAndStopRepository, JourneyRepository journeyRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.transitAndStopRepository = transitAndStopRepository;
        this.journeyRepository = journeyRepository;
    }

    @Override
    public JourneyResponseDTO addJourney(JourneyDTO journey, Long carId) {
        return mapSaveAndGetJourneyResponseDTO(journey, new Journey(), getOfficialAgencyCarById(carId));
    }

    @Override
    public JourneyResponseDTO updateJourney(JourneyDTO journey, Long journeyId, Long carId) {
        return mapSaveAndGetJourneyResponseDTO(journey, getJourney(journeyId), getOfficialAgencyCarById(carId));
    }

    @Override
    public List<JourneyResponseDTO> getAllOfficialAgencyJourneys() {
        OfficialAgency officialAgency = getOfficialAgency(verifyCurrentAuthUser());
        return journeyRepository.findAllByOrderByTimestampDescArrivalIndicatorAsc().stream()
                .filter(journey -> {
                    Car car = journey.getCar();
                    if (car instanceof Bus){
                        return ((Bus) car).getOfficialAgency().equals(officialAgency);
                    }
                    return false;
                }).map(this::mapToJourneyResponseDTO).collect(Collectors.toList());
    }

    @Override
    public JourneyResponseDTO getJourneyById(Long journeyId) {
        Journey journey = getJourney(journeyId);
        List<Bus> buses = getOfficialAgency(verifyCurrentAuthUser())
                .getBuses().stream()
                .filter(bus1 -> journey.getCar().equals(bus1)).collect(Collectors.toList());
        if (buses.isEmpty()){
            throw new ApiException("Journey\'s car not in AuthUser\'s Agency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return mapToJourneyResponseDTO(journey);
    }

    @Override
    public void addStop(Long journeyId, AddStopDTO addStopDTO) {
        Journey journey = getJourney(journeyId);
        if (journeyTerminationFilter(journey)){
            checkJourneyCarInOfficialAgency(journey);
            Set<JourneyStop> journeyStops = journey.getJourneyStops();
            System.out.println(journeyStops);
            List<TransitAndStop> transitAndStops = journeyStops.stream().map(
                    JourneyStop::getTransitAndStop
            ).collect(Collectors.toList());
            // save only if transit and stop does not already exist
            if (transitAndStops.stream()
                    .noneMatch(transitAndStop -> transitAndStop.getId()
                            .equals(addStopDTO.getTransitAndStopId()))){
                transitAndStops.add(getTransitAndStop(addStopDTO.getTransitAndStopId()));
                JourneyStop journeyStop = new JourneyStop();
                journeyStop.setAmount(addStopDTO.getAmount());
                journeyStop.setTransitAndStop(transitAndStops.get(0));
                journeyStop.setJourney(journey);
                journeyStops.add(journeyStop);
                journeyRepository.save(journey);
            }
        }
    }

    @Override
    public void deleteNonBookedJourney(Long journeyId) {
        Journey journey = getJourney(journeyId);
        if (journeyTerminationFilter(journey)){
            checkJourneyCarInOfficialAgency(journey);
            journeyRepository.delete(journey);
        }
    }

    @Override
    public void updateJourneyDepartureIndicator(Long journeyId, JourneyDepartureIndicatorDTO journeyDepartureIndicator) {
        Journey journey = getJourney(journeyId);
        if(journeyTerminationFilter(journey)){
            checkJourneyCarInOfficialAgency(journey);
            journey.setDepartureIndicator(journeyDepartureIndicator.getDepartureIndicator());
            journeyRepository.save(journey);
        }
    }

    @Override
    public void updateJourneyArrivalIndicator(Long journeyId, JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO) {
        Journey journey = getJourney(journeyId);
        if (journeyArrivalFilter(journey)){
            checkJourneyCarInOfficialAgency(journey);
            journey.setArrivalIndicator(journeyArrivalIndicatorDTO.getArrivalIndicator());
            journeyRepository.save(journey);
        }
    }

    @Override
    public JourneyResponseDTO addSharedJourney(JourneyDTO journeyDTO, Long carId) {
        return mapSaveAndGetJourneyResponseDTO(journeyDTO, new Journey(), getPersonalAgencyCarById(carId));
    }

    @Override
    public JourneyResponseDTO updateSharedJourney(JourneyDTO journeyDTO, Long journeyId, Long carId) {
        return mapSaveAndGetJourneyResponseDTO(journeyDTO, getJourney(journeyId), getPersonalAgencyCarById(carId));
    }

    /**
     * verify and return the current user in cases where user id is relevant
     * @return user
     */
    private User verifyCurrentAuthUser(){
        UserDTO authUser = userService.getCurrentAuthUser();
        // get user entity
        Optional<User> optionalUser = userRepository.findById(authUser.getId());
        if (!optionalUser.isPresent()){
            throw new ApiException("User not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }

    /**
     * Get the personal agency of user or throw api exception if agency is not found
     * @param user
     * @return officialAgency
     */
    private OfficialAgency getOfficialAgency(User user) {
        OfficialAgency officialAgency = user.getOfficialAgency();
        if (officialAgency == null){
            throw new ApiException("No Official Agency found for this user", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return officialAgency;
    }

    private PersonalAgency getPersonalAgency(User user) {
        PersonalAgency personalAgency = user.getPersonalAgency();
        if (personalAgency == null){
            throw new ApiException("No Personal Agency found for this user", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return personalAgency;
    }

    /**
     * Gets car in user's official agency. if car not found, throw car not found api exception
     * @param carId
     * @return Car
     */
    private Car getOfficialAgencyCarById(Long carId){
        List<Car> cars = getOfficialAgency(verifyCurrentAuthUser()).getBuses()
                .stream().filter(bus -> bus.getId().equals(carId)).collect(Collectors.toList());
        if (cars.isEmpty()){
            throw new ApiException("Car not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return cars.get(0);
    }

    private Car getPersonalAgencyCarById(Long carId){
        List<Car> cars = getPersonalAgency(verifyCurrentAuthUser()).getSharedRides()
                .stream().filter(sharedRide -> sharedRide.getId().equals(carId))
                .collect(Collectors.toList());
        if (cars.isEmpty()){
            throw new ApiException("Journey\'s Car not in AuthUser\'s PersonalAgency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return cars.get(0);
    }

    /**
     * throws exception if transitAndStop is not found
     * @param id
     * @return transitAndStop
     */
    private TransitAndStop getTransitAndStop(Long id){
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findById(id);
        if (!optionalTransitAndStop.isPresent()){
            throw new ApiException("TransitAndStop not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalTransitAndStop.get();
    }

    /**
     * Throws exception if transitAndStop is not found but appends message to indicate the category
     * of the transitAndStop
     * @param id
     * @param errMsg
     * @return transitAndStop
     */
    private TransitAndStop getTransitAndStopCanAppendErrMsg(Long id, String errMsg){
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findById(id);
        if (!optionalTransitAndStop.isPresent()){
            throw new ApiException(errMsg + " TransitAndStop not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalTransitAndStop.get();
    }

    private JourneyResponseDTO mapSaveAndGetJourneyResponseDTO(JourneyDTO journeyDTO, Journey journey, Car car){
        journey.setCar(car);
        TransitAndStop destinationTransitAndStop = getTransitAndStopCanAppendErrMsg(
                journeyDTO.getDestination() == null ? null: journeyDTO.getDestination().getTransitAndStopId()
                , "Destination");
        journey.setDestination(destinationTransitAndStop.getLocation());
        TransitAndStop departureTransitAndStop = getTransitAndStopCanAppendErrMsg(journeyDTO.getDepartureLocation(), "Departure");
        journey.setDepartureLocation(departureTransitAndStop.getLocation());
        Set<JourneyStop> journeyStops = journeyDTO.getTransitAndStops().stream()
                .map(journeyStop -> {
                    JourneyStop journeyStop1 = new JourneyStop();
                    journeyStop1.setTransitAndStop(getTransitAndStop(journeyStop.getTransitAndStopId()));
                    journeyStop1.setAmount(journeyStop.getAmount());
                    journeyStop1.setJourney(journey);
                    return journeyStop1;
                }).collect(Collectors.toSet());
        Set<JourneyStop> journeyStopSet = journey.getJourneyStops();
        journeyStopSet.addAll(journeyStops);
        journey.setAmount(journeyDTO.getDestination().getAmount());
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        journey.setTimestamp(TimeProvider.now());

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
        journeyResponseDTO.setTimestamp(journey.getTimestamp() == null ? null :
                Date.from(journey.getTimestamp().atZone(zoneId).toInstant()));

        journeyResponseDTO.setId(journeyRepository.save(journey).getId());
        journeyResponseDTO.setAmount(journeyDTO.getDestination().getAmount());

        return journeyResponseDTO;
    }

    private DriverDTO getDriverDTO(Driver driver) {
        DriverDTO driverDTO = new DriverDTO();
        if (driver == null){
            return null;
        }
        driverDTO.setDriverName(driver.getDriverName());
        driverDTO.setDriverLicenseNumber(driver.getDriverLicenseNumber());
        return driverDTO;
    }

    private LocationResponseDTO getLocationResponseDTO(TransitAndStop transitAndStop){
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
     * @param transitAndStop
     * @param amount
     * @return
     */
    private LocationStopResponseDTO getLocationStopResponseDTO(TransitAndStop transitAndStop, double amount){
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
     * @param id
     * @return Journey
     */
    private Journey getJourney(Long id){
        Optional<Journey> journeyOptional = journeyRepository.findById(id);
        if (!journeyOptional.isPresent()){
            throw new ApiException("Journey not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return journeyOptional.get();
    }

    private CarResponseDTO getCarResponseDTO(Car car){
        CarResponseDTO carDTO = new CarResponseDTO();
        carDTO.setId(car.getId());
        carDTO.setName(car.getName());
        carDTO.setLicensePlateNumber(car.getLicensePlateNumber());
        carDTO.setIsCarApproved(car.getIsCarApproved() == null ? false : car.getIsCarApproved());
        carDTO.setIsOfficialAgencyIndicator(car.getIsOfficialAgencyIndicator() == null ? false : car.getIsOfficialAgencyIndicator());
        carDTO.setTimestamp(car.getTimestamp() == null ? null :
                Date.from(car.getTimestamp().atZone(zoneId).toInstant()));
        return carDTO;
    }

    /**
     * Maps journey to JourneyResponseDTO
     * @param journey
     * @return JourneyResponseDTO
     */
    private JourneyResponseDTO mapToJourneyResponseDTO(Journey journey){
        JourneyResponseDTO journeyResponseDTO = new JourneyResponseDTO();
        journeyResponseDTO.setArrivalIndicator(journey.getArrivalIndicator());
        journeyResponseDTO.setCar(getCarResponseDTO(journey.getCar()));
        journeyResponseDTO.setDepartureIndicator(journey.getDepartureIndicator());
        journeyResponseDTO.setDepartureLocation(getLocationResponseDTO(
                getTransitAndStopByLocation(journey.getDepartureLocation())
        ));
        journeyResponseDTO.setDepartureTime(
                Date.from(journey.getDepartureTime() == null ? LocalDateTime.now().atZone(zoneId).toInstant() :
                        journey.getDepartureTime().atZone(zoneId).toInstant())
        );
        journeyResponseDTO.setDestination(getLocationStopResponseDTO(
                getTransitAndStopByLocation(journey.getDestination()),
                journey.getAmount()
        ));
        journeyResponseDTO.setDriver(getDriverDTO(journey.getDriver()));
        journeyResponseDTO.setEstimatedArrivalTime(journey.getEstimatedArrivalTime() == null ? null:
                Date.from(journey.getEstimatedArrivalTime().atZone(zoneId).toInstant()));
        journeyResponseDTO.setTransitAndStops(
                journey.getJourneyStops().stream().map(
                        journeyStop -> getLocationStopResponseDTO(
                                journeyStop.getTransitAndStop(), journeyStop.getAmount()
                        )
                ).collect(Collectors.toList())
        );

        journeyResponseDTO.setTimestamp(journey.getTimestamp() == null ? null :
                Date.from(journey.getTimestamp().atZone(zoneId).toInstant()));

        journeyResponseDTO.setId(journey.getId());
        journeyResponseDTO.setAmount(journey.getAmount());
        return journeyResponseDTO;
    }

    /**
     * Gets the transitAndStop for a particular location
     * @param location
     * @return
     */
    private TransitAndStop getTransitAndStopByLocation(Location location){
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findDistinctFirstByLocation(location);
        if (!optionalTransitAndStop.isPresent()){
            throw new ApiException("TransitAndStop not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalTransitAndStop.get();
    }

    /**
     * throw exception of journey is terminated
     * @param journey
     * @return boolean
     */
    private boolean journeyTerminationFilter(Journey journey){
        if (journey.getArrivalIndicator()){
            throw new ApiException("Journey already terminated", ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString(), HttpStatus.CONFLICT);
        }
        return true;
    }

    /**
     * throw exception if journey car is not in official agency
     * @param journey
     */
    private void checkJourneyCarInOfficialAgency(Journey journey) {
        List<Car> cars = getOfficialAgency(verifyCurrentAuthUser())
                .getBuses().stream().filter(bus -> journey.getCar() != null && journey.getCar().getId().equals(bus.getId())).collect(Collectors.toList());
        if (cars.isEmpty()) {
            throw new ApiException("Journey\'s car not in AuthUser\'s Agency", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * throw exception of journey is not started
     * @param journey
     * @return boolean
     */
    private boolean journeyArrivalFilter(Journey journey){
        if (journey.getDepartureIndicator()){
            throw new ApiException("Journey not started", ErrorCodes.JOURNEY_NOT_STARTED.toString(), HttpStatus.CONFLICT);
        }
        return true;
    }


}
