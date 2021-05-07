package net.gogroups.gowaka.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.domain.model.Location;
import net.gogroups.gowaka.domain.model.TransitAndStop;
import net.gogroups.gowaka.domain.repository.TransitAndStopRepository;
import net.gogroups.gowaka.dto.LocationDTO;
import net.gogroups.gowaka.dto.LocationResponseDTO;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.service.TransitAndStopService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransitAndStopServiceServiceImpl implements TransitAndStopService {

    private final TransitAndStopRepository transitAndStopRepository;

    @Override
    public LocationResponseDTO addLocation(LocationDTO locationDTO) {
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(verifyLocation(locationDTO));
        return getLocationResponseDTO(transitAndStopRepository.save(transitAndStop));
    }

    @Override
    public void updateLocation(Long id, LocationDTO locationDTO) {
        TransitAndStop transitAndStop = getTransitAndStop(id);
        restrictStopChangeToNonBookedJourneys(transitAndStop);
        transitAndStop.setLocation(verifyLocation(locationDTO));
        transitAndStopRepository.save(transitAndStop);
    }

    @Override
    public void deleteLocation(Long id) {
        transitAndStopRepository.deleteById(journeyCheck(getTransitAndStop(id)));
    }

    @Override
    public List<LocationResponseDTO> getAllLocations() {
        return transitAndStopRepository.findAll().stream()
                .filter(transitAndStop -> transitAndStop.getLocation() != null)
                .map(this::getLocationResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<LocationResponseDTO> searchByCity(String city) {
        return transitAndStopRepository.findByLocationCityIgnoreCase(city)
                .stream().filter(transitAndStop -> transitAndStop.getLocation() != null)
                .map(this::getLocationResponseDTO).collect(Collectors.toList());
    }

    private Location verifyLocation(LocationDTO locationDTO){
        Location location = new Location();
        location.setAddress(locationDTO.getAddress());
        location.setCity(locationDTO.getCity());
        location.setState(locationDTO.getState());
        location.setCountry(locationDTO.getCountry());
        location.setTlaAddress(locationDTO.getTlaAddress());
        location.setTlaCity(locationDTO.getTlaCity());
        location.setTlaState(locationDTO.getTlaState());
        location.setTlaCountry(locationDTO.getTlaCountry());
        Optional<TransitAndStop> optionalTransitAndStop = transitAndStopRepository.findDistinctFirstByLocation(location);
        if (optionalTransitAndStop.isPresent()){
            log.info("Search: {}", location.toString() );
            log.info("Found: {}", optionalTransitAndStop.get().getLocation().toString());
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

    private Long journeyCheck(TransitAndStop transitAndStop){
        if (!transitAndStop.getJourneyStops().isEmpty()){
            log.warn("Cannot delete record: \n <{}> \n has journeys", transitAndStop.toString());
            throw new ApiException(ErrorCodes.LOCATION_HAS_BOOKED_JOURNEY.getMessage(),
                    ErrorCodes.LOCATION_HAS_BOOKED_JOURNEY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return transitAndStop.getId();
    }

    private LocationResponseDTO getLocationResponseDTO(TransitAndStop transitAndStop){
        Location location = new Location();
        if (transitAndStop.getLocation() != null){
            location = transitAndStop.getLocation();
        }
        LocationResponseDTO locationResponseDTO = new LocationResponseDTO();
        locationResponseDTO.setId(transitAndStop.getId());
        locationResponseDTO.setAddress(location.getAddress());
        locationResponseDTO.setCity(location.getCity());
        locationResponseDTO.setState(location.getState());
        locationResponseDTO.setCountry(location.getCountry());
        locationResponseDTO.setTlaAddress(location.getTlaAddress());
        locationResponseDTO.setTlaCity(location.getTlaCity());
        locationResponseDTO.setTlaState(location.getTlaState());
        locationResponseDTO.setTlaCountry(location.getTlaCountry());
        return locationResponseDTO;
    }

    private void restrictStopChangeToNonBookedJourneys(TransitAndStop transitAndStop) {
        if (transitAndStop != null &&
                transitAndStop.getBookedJourneys() != null &&
                !transitAndStop.getBookedJourneys().isEmpty()) {
            throw new ApiException(ErrorCodes.LOCATION_HAS_BOOKED_JOURNEY.getMessage(),
                    ErrorCodes.LOCATION_HAS_BOOKED_JOURNEY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
