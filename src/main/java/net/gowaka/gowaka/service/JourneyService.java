package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.AddStopDTO;
import net.gowaka.gowaka.dto.JourneyDTO;
import net.gowaka.gowaka.dto.JourneyResponseDTO;

import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 17 Oct 2019
 */
public interface JourneyService {
    JourneyResponseDTO addJourney(JourneyDTO journey, Long carId);
    JourneyResponseDTO updateJourney(JourneyDTO journey, Long journeyId, Long carId);
    List<JourneyResponseDTO> getAllOfficialAgencyJourneys();
    JourneyResponseDTO getJourneyById(Long journeyId);
    void addStop(Long journeyId, AddStopDTO addStopDTO);
}
