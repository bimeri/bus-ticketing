package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.Location;
import net.gowaka.gowaka.domain.model.TransitAndStop;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.TransitAndStopRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.LocationDTO;
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

import java.util.Optional;

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
    public TransitAndStop updateLocation(Long id, LocationDTO locationDTO) {
        verifyCurrentAuthUser();
        TransitAndStop transitAndStop = getTransitAndStop(id);
        transitAndStop.setLocation(verifyLocation(locationDTO));
        return transitAndStopRepository.save(transitAndStop);
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

    private TransitAndStop getTransitAndStop(Long id){
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findById(id);
        if (!optionalTransitAndStop.isPresent()){
            throw new ApiException("TransitAndStop not found", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.NOT_FOUND);
        }
        return optionalTransitAndStop.get();
    }
}
