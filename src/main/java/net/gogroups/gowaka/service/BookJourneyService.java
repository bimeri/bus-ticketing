package net.gogroups.gowaka.service;

import io.swagger.models.auth.In;
import net.gogroups.dto.PaginatedResponse;
import net.gogroups.gowaka.dto.*;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:28 AM <br/>
 */
public interface BookJourneyService {

    PaymentUrlDTO bookJourney(Long journeyId, BookJourneyRequest bookJourneyRequest);

    void agencyUserBookJourney(Long journeyId, BookJourneyRequest bookJourneyRequest);

    List<Integer> getAllBookedSeats(Long journeyId);

    BookedJourneyStatusDTO getBookJourneyStatus(Long bookedJourneyId);

    PaginatedResponse<BookedJourneyStatusDTO> getUserBookedJourneyHistory(Integer pageNumber, Integer limit);

    void handlePaymentResponse(Long bookedJourneyId, PaymentStatusResponseDTO paymentStatusResponseDTO);

    OnBoardingInfoDTO getPassengerOnBoardingInfo(String checkedInCode);

    void checkInPassengerByCode(String checkedInCode);

    String getHtmlReceipt(Long bookedJourneyId);

    List<OnBoardingInfoDTO> getAllPassengerOnBoardingInfo(Long journeyId);

}
