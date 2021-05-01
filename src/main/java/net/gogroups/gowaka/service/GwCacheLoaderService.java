package net.gogroups.gowaka.service;

import net.gogroups.gowaka.dto.JourneyResponseDTO;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/20/21 8:52 PM <br/>
 */
public interface GwCacheLoaderService {

    void seatsChange(Long journeyId, List<Integer> bookedSeats);

    void addUpdateJourney(JourneyResponseDTO journeyResponseDTO);

    void deleteJourneyJourney(Long agencyId, Long branchId, Long journeyId);
}
