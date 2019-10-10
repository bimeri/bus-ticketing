package net.gowaka.gowaka.service;

import net.gowaka.gowaka.domain.model.TransitAndStop;
import net.gowaka.gowaka.dto.LocationDTO;
import net.gowaka.gowaka.dto.LocationResponseDTO;

import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */
public interface TransitAndStopService {
    TransitAndStop addLocation(LocationDTO locationDTO);
    void updateLocation(Long id, LocationDTO locationDTO);
    void deleteLocation(Long id);
    List<LocationResponseDTO> getAllLocations();
    List<LocationResponseDTO> searchByCity(String city);
}
