package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.*;

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
    void deleteNonBookedJourney(Long journeyId);
    void updateJourneyDepartureIndicator(Long journeyId, JourneyDepartureIndicatorDTO journeyDepartureIndicator);
    void updateJourneyArrivalIndicator(Long journeyId, JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO);
    JourneyResponseDTO addSharedJourney(JourneyDTO journeyDTO, Long carId);
    JourneyResponseDTO updateSharedJourney(JourneyDTO journeyDTO, Long journeyId, Long carId);
    JourneyResponseDTO getSharedJourneyById(Long journeyId);
    List<JourneyResponseDTO> getAllPersonalAgencyJourneys();
    void updateSharedJourneyDepartureIndicator(Long journeyId, JourneyDepartureIndicatorDTO journeyDepartureIndicator);
    void updateSharedJourneyArrivalIndicator(Long journeyId, JourneyArrivalIndicatorDTO journeyArrivalIndicator);
}
