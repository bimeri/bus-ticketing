package net.gogroups.gowaka.service;

import net.gogroups.dto.PaginatedResponse;
import net.gogroups.gowaka.domain.model.Journey;
import net.gogroups.gowaka.dto.*;

import java.util.List;

/**
 * @author Nnouka Stephen
 * @date 17 Oct 2019
 */
public interface JourneyService {

    JourneyResponseDTO addJourney(JourneyDTO journey, Long carId);

    JourneyResponseDTO updateJourney(JourneyDTO journey, Long journeyId, Long carId);

    List<JourneyResponseDTO> getAllOfficialAgencyJourneys();

    PaginatedResponse<JourneyResponseDTO> getOfficialAgencyJourneys(Integer pageNumber, Integer limit, Long branchId);

    JourneyResponseDTO getJourneyById(Long journeyId);

    JourneyResponseDTO getAJourneyById(Long journeyId);

    void addStop(Long journeyId, AddStopDTO addStopDTO);

    void deleteNonBookedJourney(Long journeyId);

    void updateJourneyDepartureIndicator(Long journeyId, JourneyDepartureIndicatorDTO journeyDepartureIndicator);

    void updateJourneyArrivalIndicator(Long journeyId, JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO);

    void removeNonBookedStop(Long journeyId, Long stopId);

    JourneyResponseDTO addSharedJourney(JourneyDTO journeyDTO, Long carId);

    JourneyResponseDTO updateSharedJourney(JourneyDTO journeyDTO, Long journeyId, Long carId);

    JourneyResponseDTO getSharedJourneyById(Long journeyId);

    List<JourneyResponseDTO> getAllPersonalAgencyJourneys();

    void updateSharedJourneyDepartureIndicator(Long journeyId, JourneyDepartureIndicatorDTO journeyDepartureIndicator);

    void updateSharedJourneyArrivalIndicator(Long journeyId, JourneyArrivalIndicatorDTO journeyArrivalIndicator);

    void deleteNonBookedSharedJourney(Long journeyId);

    void addStopToPersonalAgency(Long journeyId, AddStopDTO addStopDTO);

    List<JourneyResponseDTO> searchJourney(Long departureLocationId, Long destinationLocationId, String time);

    List<JourneyResponseDTO> searchJourney();
    List<JourneyResponseDTO> searchAllAvailableJourney();

    // service level methods

    void checkJourneyCarInOfficialAgency(Journey journey);
}
