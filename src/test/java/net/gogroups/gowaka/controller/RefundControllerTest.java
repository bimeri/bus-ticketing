package net.gogroups.gowaka.controller;


import net.gogroups.gowaka.dto.RefundDTO;
import net.gogroups.gowaka.dto.RequestRefundDTO;
import net.gogroups.gowaka.dto.ResponseRefundDTO;
import net.gogroups.gowaka.service.RefundService;
import net.gogroups.security.accessconfig.UserDetailsImpl;
import net.gogroups.security.service.AuthorizedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/17/20 8:25 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
class RefundControllerTest {
    private RefundController refundController;

    @Mock
    private RefundService mockRefundService;
    @Mock
    private AuthorizedUserService mockAuthorizedUserService;


    @BeforeEach
    void setUp() {
        refundController = new RefundController(mockRefundService, mockAuthorizedUserService);
    }

    @Test
    void requestRefund_call_refundService() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("123");
        when(mockAuthorizedUserService.getUserDetails())
                .thenReturn(userDetails);

        RequestRefundDTO requestRefundDTO = new RequestRefundDTO(1L, 2L, "please refund.");
        ResponseEntity<Void> response = refundController.requestRefund(requestRefundDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(mockRefundService).requestRefund(requestRefundDTO, "123");
    }

    @Test
    void responseRefund_call_refundService() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("123");
        when(mockAuthorizedUserService.getUserDetails())
                .thenReturn(userDetails);

        ResponseRefundDTO responseRefundDTO = new ResponseRefundDTO(true, "yes refund.", 1000.0);
        ResponseEntity<Void> response = refundController.responseRefund(1L, responseRefundDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(mockRefundService).responseRefund(1L, responseRefundDTO, "123");
    }

    @Test
    void getUserRefundRequest_call_refundService() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("123");
        when(mockAuthorizedUserService.getUserDetails())
                .thenReturn(userDetails);
        when(mockRefundService.getUserRefund(anyLong(), anyString()))
                .thenReturn(new RefundDTO());

        ResponseEntity<RefundDTO> response = refundController.getUserRefundRequest(1L);
        assertThat(response
                .getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockRefundService).getUserRefund(1L, "123");
    }

    @Test
    void getAllJourneyRefunds_call_refundService() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("123");
        when(mockAuthorizedUserService.getUserDetails())
                .thenReturn(userDetails);
        when(mockRefundService.getAllJourneyRefunds(anyLong(), anyString()))
                .thenReturn(Collections.singletonList(new RefundDTO()));

        ResponseEntity<List<RefundDTO>> response = refundController.getAllJourneyRefunds(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockRefundService).getAllJourneyRefunds(1L, "123");
    }

    @Test
    void refunded_call_refundService() {

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId("123");
        when(mockAuthorizedUserService.getUserDetails())
                .thenReturn(userDetails);

        ResponseEntity<Void> response = refundController.refunded(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(mockRefundService).refunded(1L, "123");
    }

}
