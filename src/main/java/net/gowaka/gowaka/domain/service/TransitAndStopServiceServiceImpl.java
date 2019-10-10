package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.Location;
import net.gowaka.gowaka.domain.model.TransitAndStop;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.TransitAndStopRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.LocationDTO;
import net.gowaka.gowaka.dto.LocationResponseDTO;
import net.gowaka.gowaka.dto.UserDTO;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.service.TransitAndStopService;
import net.gowaka.gowaka.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */
@Service
public class TransitAndStopServiceServiceImpl implements TransitAndStopService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private TransitAndStopRepository transitAndStopRepository;
    private UserRepository userRepository;
    private UserService userService;


    @Autowired
    public TransitAndStopServiceServiceImpl(TransitAndStopRepository transitAndStopRepository, UserRepository userRepository, UserService userService) {
        this.transitAndStopRepository = transitAndStopRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public TransitAndStop addLocation(LocationDTO locationDTO) {
        verifyCurrentAuthUser();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(verifyLocation(locationDTO));
        return transitAndStopRepository.save(transitAndStop);
    }

    @Override
    public void updateLocation(Long id, LocationDTO locationDTO) {
        verifyCurrentAuthUser();
        TransitAndStop transitAndStop = getTransitAndStop(id);
        transitAndStop.setLocation(verifyLocation(locationDTO));
        transitAndStopRepository.save(transitAndStop);
    }

    @Override
    public void deleteLocation(Long id) {
        verifyCurrentAuthUser();
        safeDelete(id);
    }

    @Override
    public List<LocationResponseDTO> getAllLocations() {
        List<TransitAndStop> transitAndStopList = transitAndStopRepository.findAll();
        return transitAndStopList != null ? transitAndStopList.stream()
                .filter(transitAndStop -> transitAndStop.getLocation() != null)
                .map(this::getLocationResponseDTO).collect(Collectors.toList()) : Collections.emptyList();
    }

    @Override
    public List<LocationResponseDTO> searchByCity(String city) {
        return transitAndStopRepository.findByLocationCityIgnoreCase(city)
                .stream().filter(transitAndStop -> transitAndStop.getLocation() != null)
                .map(this::getLocationResponseDTO).collect(Collectors.toList());
    }

    private User verifyCurrentAuthUser(){
        UserDTO authUser = userService.getCurrentAuthUser();
        // get user entity
        Optional<User> optionalUser = userRepository.findById(authUser.getId());
        if (!optionalUser.isPresent()){
            throw new ApiException("User not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }

    private Location verifyLocation(LocationDTO locationDTO){
        Location location = new Location();
        location.setAddress(locationDTO.getAddress());
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setCountry(locationDTO.getCountry());
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findDistinctByLocation(location);
        if (optionalTransitAndStop.isPresent()){
            logger.info("Search: {}", location.toString() );
            logger.info("Found: {}", optionalTransitAndStop.get().getLocation().toString());
            throw new ApiException("TransitAndStop already Exists", ErrorCodes.TRANSIT_AND_STOP_ALREADY_IN_USE.toString(), HttpStatus.CONFLICT);
        }
        return location;
    }

    private void safeDelete(Long id){
        transitAndStopRepository.deleteById(journeyCheck(getTransitAndStop(id)));
    }

    private TransitAndStop getTransitAndStop(Long id){
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findById(id);
        if (!optionalTransitAndStop.isPresent()){
            throw new ApiException("TransitAndStop not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalTransitAndStop.get();
    }

    private Long journeyCheck(TransitAndStop transitAndStop){
        if (!transitAndStop.getJourneys().isEmpty()){
            logger.warn("Cannot delete record: \n <{}> \n has journeys", transitAndStop.toString());
            throw new ApiException("Cannot delete record for any existing journey", ErrorCodes.VALIDATION_ERROR.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return transitAndStop.getId();
    }

    private LocationResponseDTO getLocationResponseDTO(TransitAndStop transitAndStop){
        Location location = transitAndStop.getLocation();
        LocationResponseDTO locationResponseDTO = new LocationResponseDTO();
        locationResponseDTO.setId(transitAndStop.getId());
        locationResponseDTO.setAddress(location.getAddress());
        locationResponseDTO.setCity(location.getCity());
        locationResponseDTO.setState(location.getState());
        locationResponseDTO.setCountry(location.getCountry());
        return locationResponseDTO;
    }
}
