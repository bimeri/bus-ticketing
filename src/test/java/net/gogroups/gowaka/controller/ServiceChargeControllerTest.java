package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.ServiceChargeDTO;
import net.gogroups.gowaka.service.ServiceChargeService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 6:41 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
public class ServiceChargeControllerTest {

    @Mock
    private ServiceChargeService mockServiceChargeService;

    private ServiceChargeController serviceChargeController;

    @BeforeEach
    void setup() {
        serviceChargeController = new ServiceChargeController(mockServiceChargeService);
    }

    @Test
    void getServiceCharge_call_ServiceChargeService() {

        when(mockServiceChargeService.getServiceCharges())
                .thenReturn(Collections.singletonList(new ServiceChargeDTO()));
        ResponseEntity<List<ServiceChargeDTO>> response = serviceChargeController.getServiceCharge();
        verify(mockServiceChargeService).getServiceCharges();
        assertThat(response.getBody()).isInstanceOf(List.class);
        assertThat(response.getBody().get(0)).isInstanceOf(ServiceChargeDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateServiceCharge_call_ServiceChargeService() {

        ServiceChargeDTO serviceChargeDTO = new ServiceChargeDTO();
        serviceChargeDTO.setPercentageCharge(10.0);

        ResponseEntity<?> response = serviceChargeController.updateServiceCharge(serviceChargeDTO);
        verify(mockServiceChargeService).updateServiceCharge(serviceChargeDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
