package net.gogroups.gowaka.service;

import net.gogroups.gowaka.domain.model.TransitAndStop;
import net.gogroups.gowaka.dto.LocationDTO;
import net.gogroups.gowaka.dto.LocationResponseDTO;

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
