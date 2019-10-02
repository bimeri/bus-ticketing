package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.Bus;
import net.gowaka.gowaka.domain.model.OfficialAgency;
import net.gowaka.gowaka.domain.model.SharedRide;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.ResponseBusDTO;
import net.gowaka.gowaka.dto.SharedRideDTO;
import net.gowaka.gowaka.dto.UserDTO;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.service.CarService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
 @RunWith(MockitoJUnitRunner.class)
public class CarServiceImplTest {
     @Mock
     private CarRepository mockCarRepository;
     @Mock
     private UserService mockUserService;
     @Mock
     private UserRepository mockUserRepository;
     private CarService carService;

     @Mock
     private OfficialAgency mockOfficialAgency;

     @Rule
     public ExpectedException expectedException = ExpectedException.none();

     @Before
     public void setup() throws Exception{
         carService = new CarServiceImpl(mockCarRepository, mockUserService, mockUserRepository);
     }

     @Test
     public void official_agency_should_add_car(){
         Bus bus = new Bus();
         bus.setId(1L);
         bus.setNumberOfSeats(0);
         User user = new User();
         user.setUserId("1");
         BusDTO busDTO = new BusDTO();
         busDTO.setNumberOfSeats(0);
         UserDTO userDTO = new UserDTO();
         userDTO.setId("1");
         String []roles = {"ROLE_AGENCY_MANAGER"};
         userDTO.setRoles(Arrays.asList(roles));
         when(mockCarRepository.save(any())).thenReturn(bus);
         when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
         when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
         carService.addOfficialAgencyBus(busDTO);
         verify(mockUserService).getCurrentAuthUser();
         verify(mockUserRepository).findById("1");
         verify(mockCarRepository).save(bus);
     }

     @Test
     public void official_agency_should_throw_resource_not_found_exception(){
         BusDTO busDTO = new BusDTO();
         UserDTO userDTO = new UserDTO();
         when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
         expectedException.expect(ApiException.class);
         expectedException.expectMessage("User not found");
         expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
         carService.addOfficialAgencyBus(busDTO);
     }

     @Test
     public void private_agency_should_add_sharedRide(){
         SharedRide sharedRide = new SharedRide();
         sharedRide.setId(1L);
         User user = new User();
         user.setUserId("1");
         SharedRideDTO sharedRideDTO = new SharedRideDTO();
         UserDTO userDTO = new UserDTO();
         userDTO.setId("1");
         when(mockCarRepository.save(any())).thenReturn(sharedRide);
         when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
         when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
         carService.addSharedRide(sharedRideDTO);
         verify(mockUserService).getCurrentAuthUser();
         verify(mockUserRepository).findById("1");
         verify(mockCarRepository).save(sharedRide);
     }

     @Test
     public void private_agency_should_throw_resource_not_found_exception(){
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        UserDTO userDTO = new UserDTO();
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("User not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        carService.addSharedRide(sharedRideDTO);
     }

    @Test
    public void official_agency_manager_should_get_all_buses(){
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Happy");
        Bus bus1 = new Bus();
        bus1.setId(2L);
        bus1.setName("Angry");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        User user = new User();
        user.setUserId("1");
        user.setOfficialAgency(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockOfficialAgency.getBuses()).thenReturn(new ArrayList<>(Arrays.asList(bus, bus1)));
        List<ResponseBusDTO> responseBusDTOS = carService.getAllOfficialAgencyBuses();
        assertThat(responseBusDTOS.get(0).getId(), is(bus.getId()));
        assertThat(responseBusDTOS.get(0).getName(), is(bus.getName()));
        assertThat(responseBusDTOS.get(1).getId(), both(not(bus.getId())).and(is(bus1.getId())));
        assertThat(responseBusDTOS.get(1).getName(), is(bus1.getName()));
    }

    @Test
    public void official_agency_get_buses_should_throw_resource_not_found_exception(){
        UserDTO userDTO = new UserDTO();
        User user = new User();
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("No Agency found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        carService.getAllOfficialAgencyBuses();
    }

    @Test
    public void official_agency_get_buses_should_throw_resource_not_found_exception_with_204_no_content(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        User user = new User();
        user.setUserId("1");
        user.setOfficialAgency(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Agency is Empty");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        carService.getAllOfficialAgencyBuses();
    }
/*
     @Test
     public void should_approve_disapprove_car(){
         Car car = new Bus();
         ApproveCarDTO approveCarDTO = new ApproveCarDTO();


         car.setId(1L);
         when(mockCarRepository.findById(anyLong())).thenReturn(Optional.of(car));
         when(mockCarRepository.save(any())).thenReturn(car);

         approveCarDTO.setApprove(true);
         carService.approve(approveCarDTO, 1L);
         verify(mockCarRepository).findById(1L);
         verify(mockCarRepository).save(car);
         assertTrue(car.getIsCarApproved());

         approveCarDTO.setApprove(false);
         carService.approve(approveCarDTO, 1L);
         assertFalse(car.getIsCarApproved());
     }

     @Test(expected = ApiException.class)
     public void should_throw_car_not_found_api_exception(){
         Car car = new Bus();
         ApproveCarDTO approveCarDTO = new ApproveCarDTO();
         car.setId(1L);
         when(mockCarRepository.findById(anyLong())).thenReturn(Optional.empty());
         approveCarDTO.setApprove(true);
         carService.approve(approveCarDTO, 1L);
         verify(mockCarRepository).findById(1L);
     }

 */
}
