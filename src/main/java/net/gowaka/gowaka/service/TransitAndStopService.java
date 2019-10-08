package net.gowaka.gowaka.service;

import net.gowaka.gowaka.domain.model.TransitAndStop;
import net.gowaka.gowaka.dto.LocationDTO;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */
public interface TransitAndStopService {
    TransitAndStop addLocation(LocationDTO locationDTO);
}
