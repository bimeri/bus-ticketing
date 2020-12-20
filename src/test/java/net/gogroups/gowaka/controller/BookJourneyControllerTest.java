package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.domain.model.BookedJourney;
import net.gogroups.gowaka.domain.service.HtmlToPdfGenarator;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.BookJourneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:40 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
public class BookJourneyControllerTest {

    @Mock
    private BookJourneyService mockBookJourneyService;
    @Mock
    private HtmlToPdfGenarator mockHtmlToPdfGenarator;

    private BookJourneyController bookJourneyController;

    @BeforeEach
    public void setUp() {
        bookJourneyController = new BookJourneyController(mockBookJourneyService, mockHtmlToPdfGenarator);
    }

    @Test
    void bookJourney_callsBookJourneyService() {
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        PaymentUrlDTO paymentUrl = new PaymentUrlDTO();
        paymentUrl.setPaymentUrl("http://payamgo.com/xyz");
        when(mockBookJourneyService.bookJourney(anyLong(), any(BookJourneyRequest.class)))
                .thenReturn(paymentUrl);

        ResponseEntity<PaymentUrlDTO> paymentUrlDTOResponseEntity = bookJourneyController.bookJourney(1L, bookJourneyRequest);
        verify(mockBookJourneyService).bookJourney(1L, bookJourneyRequest);
        assertThat(paymentUrlDTOResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(paymentUrlDTOResponseEntity.getBody()).isInstanceOf(PaymentUrlDTO.class);
        assertThat(paymentUrlDTOResponseEntity.getBody().getPaymentUrl()).isEqualTo("http://payamgo.com/xyz");

    }

    @Test
    void agencyUserBookJourney_callsBookJourneyService() {

        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setTransitAndStopId(1L);

        ResponseEntity<?> response = bookJourneyController.agencyUserBookJourney(1L, bookJourneyRequest);
        verify(mockBookJourneyService).agencyUserBookJourney(1L, bookJourneyRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

    @Test
    void getAllBookedSeats_callsBookJourneyService() {

        when(mockBookJourneyService.getAllBookedSeats(anyLong()))
                .thenReturn(Arrays.asList(1, 2, 3, 4));

        ResponseEntity<List<Integer>> allBookedSeats = bookJourneyController.getAllBookedSeats(1L);
        verify(mockBookJourneyService).getAllBookedSeats(1L);
        assertThat(allBookedSeats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allBookedSeats.getBody().size()).isEqualTo(4);
        assertThat(allBookedSeats.getBody().get(0)).isEqualTo(1);
        assertThat(allBookedSeats.getBody().get(1)).isEqualTo(2);
        assertThat(allBookedSeats.getBody().get(2)).isEqualTo(3);
        assertThat(allBookedSeats.getBody().get(3)).isEqualTo(4);

    }

    @Test
    void getBookJourneyStatus_callsBookJourneyService() {

        BookedJourneyStatusDTO bookedJourneyStatus = new BookedJourneyStatusDTO();
        bookedJourneyStatus.setId(10L);
        when(mockBookJourneyService.getBookJourneyStatus(anyLong()))
                .thenReturn(bookedJourneyStatus);

        ResponseEntity<BookedJourneyStatusDTO> bookJourneyStatus = bookJourneyController.getBookJourneyStatus(1L);
        verify(mockBookJourneyService).getBookJourneyStatus(1L);
        assertThat(bookJourneyStatus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bookJourneyStatus.getBody()).isEqualTo(bookedJourneyStatus);

    }

    @Test
    void downloadReceipt_callsBookJourneyServiceAndHtmlToPdfGenarator() throws Exception {

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);

        when(mockBookJourneyService.getHtmlReceipt(anyLong()))
                .thenReturn("<html></html>");
        when(mockHtmlToPdfGenarator.createPdf(anyString(), anyString()))
                .thenReturn(File.createTempFile("myPdfFile", ".pdf"));

        ResponseEntity<Resource> output = bookJourneyController.downloadReceipt(1L);
        verify(mockBookJourneyService).getHtmlReceipt(1L);
        verify(mockHtmlToPdfGenarator).createPdf(htmlCaptor.capture(), filenameCaptor.capture());

        assertThat(htmlCaptor.getValue()).isEqualTo("<html></html>");
        assertThat(filenameCaptor.getValue()).contains("GowakaReceipt_");

        assertThat(output.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    void handlePaymentResponse_callsBookJourneyService() {

        PaymentStatusResponseDTO paymentStatusResponseDTO = new PaymentStatusResponseDTO();
        paymentStatusResponseDTO.setProcessingNumber("12345");

        ResponseEntity<?> responseEntity = bookJourneyController.handlePaymentResponse(1L, paymentStatusResponseDTO);
        verify(mockBookJourneyService).handlePaymentResponse(1L, paymentStatusResponseDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

    @Test
    void bookedJourneyHistory_callsBookJourneyService() {

        when(mockBookJourneyService.getUserBookedJourneyHistory())
                .thenReturn(Collections.singletonList(new BookedJourneyStatusDTO()));

        ResponseEntity<List<BookedJourneyStatusDTO>> listResponseEntity = bookJourneyController.bookedJourneyHistory();
        assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponseEntity.getBody()).isInstanceOf(List.class);

    }

    @Test
    void getOnBoardingInfoResponse_callsBookJourneyService() {
        when(mockBookJourneyService.getPassengerOnBoardingInfo(anyString()))
                .thenReturn(new OnBoardingInfoDTO(new BookedJourney()));
        ResponseEntity<OnBoardingInfoDTO> responseEntity = bookJourneyController.getOnBoardingInfoResponse("someCode");
        verify(mockBookJourneyService).getPassengerOnBoardingInfo("someCode");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isInstanceOf(OnBoardingInfoDTO.class);
    }

    @Test
    void checkInPassengerByCode_callsBookJourneyService() {
        bookJourneyController.checkInPassengerByCode(new CodeDTO("someCode"));
        verify(mockBookJourneyService).checkInPassengerByCode("someCode");
    }

}
