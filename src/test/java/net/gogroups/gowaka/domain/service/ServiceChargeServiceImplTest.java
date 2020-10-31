package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.model.ServiceCharge;
import net.gogroups.gowaka.domain.repository.ServiceChargeServiceRepository;
import net.gogroups.gowaka.dto.ServiceChargeDTO;
import net.gogroups.gowaka.service.ServiceChargeService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 7:00 AM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceChargeServiceImplTest {

    @Mock
    private ServiceChargeServiceRepository mockServiceChargeServiceRepository;

    private ServiceChargeService serviceChargeService;


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        serviceChargeService = new ServiceChargeServiceImpl(mockServiceChargeServiceRepository);
    }

    @Test
    public void getServiceCharges_returnListOf_serviceCharges() {

        ServiceCharge serviceCharge = new ServiceCharge();
        serviceCharge.setId("sc-id");
        serviceCharge.setPercentageCharge(5.0);
        when(mockServiceChargeServiceRepository.findAll())
                .thenReturn(Collections.singletonList(serviceCharge));

        List<ServiceChargeDTO> serviceChargeDTOs = serviceChargeService.getServiceCharges();
        verify(mockServiceChargeServiceRepository).findAll();
        assertThat(serviceChargeDTOs.get(0).getId()).isEqualTo("sc-id");
        assertThat(serviceChargeDTOs.get(0).getPercentageCharge()).isEqualTo(5.0);

    }

    @Test
    public void updateServiceCharge_throwsException_whenIdDont_exist() {

        ServiceChargeDTO serviceChargeDTO = new ServiceChargeDTO();
        serviceChargeDTO.setId("sc-id");
        serviceChargeDTO.setPercentageCharge(5.0);
        when(mockServiceChargeServiceRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        expectedException.expectMessage("Service charge not found");
        serviceChargeService.updateServiceCharge(serviceChargeDTO);
        verify(mockServiceChargeServiceRepository).findById("sc-id");

    }

    @Test
    public void updateServiceCharge_update_whenIdExist() {

        ArgumentCaptor<ServiceCharge> serviceChargeArgumentCaptor = ArgumentCaptor.forClass(ServiceCharge.class);

        ServiceChargeDTO serviceChargeDTO = new ServiceChargeDTO();
        serviceChargeDTO.setId("sc-id");
        serviceChargeDTO.setPercentageCharge(5.0);
        when(mockServiceChargeServiceRepository.findById(anyString()))
                .thenReturn(Optional.of(new ServiceCharge()));

        serviceChargeService.updateServiceCharge(serviceChargeDTO);
        verify(mockServiceChargeServiceRepository).findById("sc-id");
        verify(mockServiceChargeServiceRepository).save(serviceChargeArgumentCaptor.capture());
        assertThat(serviceChargeArgumentCaptor.getValue().getPercentageCharge()).isEqualTo(5.0);

    }
}
