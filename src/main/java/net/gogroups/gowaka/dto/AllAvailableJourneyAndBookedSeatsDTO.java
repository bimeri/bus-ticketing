package net.gogroups.gowaka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/20/21 7:35 PM <br/>
 */
@Data
public class AllAvailableJourneyAndBookedSeatsDTO {

    private List<JourneyResponseDTO> journeys = new ArrayList<>();
    private List<BookedSeat> bookedSeats = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookedSeat {
        private Long journeyId;
        List<Integer> bookedSeats = new ArrayList<>();
    }
}
