package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.*;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:28 AM <br/>
 */
public interface BookJourneyService {

    PaymentUrlDTO bookJourney(Long journeyId, BookJourneyRequest bookJourneyRequest);

    List<Integer> getAllBookedSeats(Long journeyId);

    BookedJourneyStatusDTO getBookJourneyStatus(Long bookedJourneyId);

    List<BookedJourneyStatusDTO> getUserBookedJourneyHistory();

    void handlePaymentResponse(Long bookedJourneyId, PaymentStatusResponseDTO paymentStatusResponseDTO);

    OnBoardingInfoDTO getPassengerOnBoardingInfo(String checkedInCode);

    void checkInPassengerByCode(String checkedInCode);

}
