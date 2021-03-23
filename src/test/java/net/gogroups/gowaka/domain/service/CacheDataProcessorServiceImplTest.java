package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.JourneyRepository;
import net.gogroups.gowaka.domain.repository.PassengerRepository;
import net.gogroups.gowaka.dto.AllAvailableJourneyAndBookedSeatsDTO;
import net.gogroups.gowaka.service.CacheDataProcessorService;
import net.gogroups.storage.constants.FileAccessType;
import net.gogroups.storage.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static net.gogroups.payamgo.constants.PayAmGoPaymentStatus.COMPLETED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/20/21 5:44 PM <br/>
 */
@ExtendWith(MockitoExtension.class)
class CacheDataProcessorServiceImplTest {

    private CacheDataProcessorService cacheDataProcessorService;

    @Mock
    private JourneyRepository mockJourneyRepository;
    @Mock
    private PassengerRepository mockPassengerRepository;
    @Mock
    private FileStorageService mockFileStorageService;

    @BeforeEach
    void setUp() {
        cacheDataProcessorService = new CacheDataProcessorServiceImpl(mockJourneyRepository, mockPassengerRepository, mockFileStorageService);
    }

    @Test
    void getAllAvailableJourneys_call_repository() {

        Journey journey = new Journey();
        journey.setId(10L);
        journey.setDepartureIndicator(Boolean.FALSE);
        AgencyBranch agencyBranch = new AgencyBranch();
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setLogo("logo-file-name.png");
        agencyBranch.setOfficialAgency(officialAgency);
        journey.setAgencyBranch(agencyBranch);
        journey.setDepartureTime(LocalDateTime.now());
        Bus bus = new Bus();
        bus.setNumberOfSeats(30);
        journey.setCar(bus);
        BookedJourney bookedJourney = new BookedJourney();
        Passenger passenger = new Passenger();
        passenger.setSeatNumber(13);
        bookedJourney.setPassengers(Collections.singletonList(passenger));
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus(COMPLETED.name());
        paymentTransaction.setCreatedAt(LocalDateTime.now());
        bookedJourney.setPaymentTransaction(paymentTransaction);
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));
        when(mockJourneyRepository.findByDepartureIndicatorFalse())
                .thenReturn(Collections.singletonList(journey));
        when(mockFileStorageService.getFilePath(anyString(), anyString(), any()))
                .thenReturn("http://localhost/logo.png");

        AllAvailableJourneyAndBookedSeatsDTO allAvailableJourneys = cacheDataProcessorService.getAllAvailableJourneys();
        verify(mockJourneyRepository).findByDepartureIndicatorFalse();
        verify(mockFileStorageService).getFilePath("logo-file-name.png", "", FileAccessType.PROTECTED);
        assertThat(allAvailableJourneys.getJourneys().get(0).getDepartureIndicator()).isFalse();
        assertThat(allAvailableJourneys.getJourneys().get(0).getCar().getAgencyLogo()).isEqualTo("http://localhost/logo.png");
        assertThat(allAvailableJourneys.getJourneys().get(0).getCar().getNumberOfSeat()).isEqualTo(30);
        assertThat(allAvailableJourneys.getBookedSeats().get(0).getJourneyId()).isEqualTo(10L);
        assertThat(allAvailableJourneys.getBookedSeats().get(0).getBookedSeats()).isEqualTo(Collections.singletonList(13));
    }

}
