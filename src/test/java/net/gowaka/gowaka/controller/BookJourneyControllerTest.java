package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.BookJourneyRequest;
import net.gowaka.gowaka.dto.PaymentUrlDTO;
import net.gowaka.gowaka.service.BookJourneyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 3/25/20 9:40 AM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class BookJourneyControllerTest {

    @Mock
    private BookJourneyService mockBookJourneyService;

    private BookJourneyController bookJourneyController;
    @Before
    public void setUp() throws Exception {
        bookJourneyController = new BookJourneyController(mockBookJourneyService);
    }

    @Test
    public void bookJourney_callsBookJourneyService() {
        BookJourneyRequest bookJourneyRequest = new BookJourneyRequest();
        bookJourneyRequest.setPassengerName("Jesus Christ");
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
}