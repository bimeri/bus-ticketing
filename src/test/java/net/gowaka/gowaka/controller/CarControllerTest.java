package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.domain.model.Bus;
import net.gowaka.gowaka.domain.model.Car;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.OfficialAgencyRepository;
import net.gowaka.gowaka.domain.repository.PersonalAgencyRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.domain.service.CarServiceImpl;
import net.gowaka.gowaka.dto.ApproveCarDTO;
import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.UserDTO;
import net.gowaka.gowaka.service.CarService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class CarControllerTest {
    @Mock
    private CarService mockCarService;
    @Mock
    private UserService mockUserService;
    @Mock
    private UserRepository mockUserRepository;

    private CarController carController;
    private CarService carService;

    @Mock
    private CarRepository mockCarRepository;

    @Before
    public void setup(){
        carController = new CarController(mockCarService);
        carService = new CarServiceImpl(mockCarRepository, mockUserService, mockUserRepository);
    }

    @Test
    public void should_call_car_service_add_agency_bus_method(){
        BusDTO busDTO = new BusDTO();
        carController.addAgencyBus(busDTO);
        verify(mockCarService).addOfficialAgencyBus(busDTO);
    }

/*
    @Test
    public void should_call_car_service_approve_method(){
        ApproveCarDTO approveCarDTO = new ApproveCarDTO();
        carController.shallApprove(approveCarDTO, "1");
        verify(mockCarService).approve(approveCarDTO, 1L);
    }

    @Test
    public void should_return_204_no_content_status_code(){
        Car car = new Bus();
        ApproveCarDTO approveCarDTO = new ApproveCarDTO();
        carController = new CarController(carService);

        car.setId(1L);
        when(mockCarRepository.findById(anyLong())).thenReturn(Optional.of(car));
        when(mockCarRepository.save(any())).thenReturn(car);

        approveCarDTO.setApprove(true);
        ResponseEntity<Object> responseEntity = carController.shallApprove(approveCarDTO, "1");
        verify(mockCarRepository).findById(1L);
        verify(mockCarRepository).save(car);
        assertTrue(car.getIsCarApproved());
        assertTrue(responseEntity.getStatusCode().toString().equals("204 NO_CONTENT"));

        approveCarDTO.setApprove(false);
        carController.shallApprove(approveCarDTO, "1");
        assertFalse(car.getIsCarApproved());
        assertTrue(responseEntity.getStatusCode().toString().equals("204 NO_CONTENT"));
    }

 */
}
