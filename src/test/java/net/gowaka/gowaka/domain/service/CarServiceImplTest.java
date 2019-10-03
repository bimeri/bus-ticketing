package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.*;
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
import static org.junit.Assert.*;
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
     @Mock
     private User user;

     private CarService carService;

     @Mock
     private OfficialAgency mockOfficialAgency;

     @Mock
     private PersonalAgency mockPersonalAgency;

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
         user.setUserId("1");
         BusDTO busDTO = new BusDTO();
         UserDTO userDTO = new UserDTO();
         userDTO.setId("1");
         when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
         when(mockCarRepository.save(any())).thenReturn(bus);
         when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
         when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
         carService.addOfficialAgencyBus(busDTO);
         verify(mockUserService).getCurrentAuthUser();
         verify(mockUserRepository).findById("1");
         verify(mockCarRepository).save(any());
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
     public void personal_agency_should_add_sharedRide(){
         SharedRide sharedRide = new SharedRide();
         sharedRide.setId(1L);
         user.setUserId("1");
         SharedRideDTO sharedRideDTO = new SharedRideDTO();
         UserDTO userDTO = new UserDTO();
         userDTO.setId("1");
         when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
         when(mockCarRepository.save(any())).thenReturn(sharedRide);
         when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
         when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
         carService.addSharedRide(sharedRideDTO);
         verify(mockUserService).getCurrentAuthUser();
         verify(mockUserRepository).findById("1");
         verify(mockCarRepository).save(any());
     }

     @Test
     public void personal_agency_should_throw_resource_not_found_exception(){
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        UserDTO userDTO = new UserDTO();
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("User not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        carService.addSharedRide(sharedRideDTO);
     }

    @Test
    public void personal_agency_add_sharedRide_should_throw_resource_not_found_exception(){
        UserDTO userDTO = new UserDTO();
        User user = new User();
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("No Personal Agency found for this user");
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
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockOfficialAgency.getBuses()).thenReturn(new ArrayList<>(Arrays.asList(bus, bus1)));
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        List<ResponseBusDTO> responseBusDTOS = carService.getAllOfficialAgencyBuses();
        assertThat(responseBusDTOS.get(0).getId(), is(bus.getId()));
        assertThat(responseBusDTOS.get(0).getName(), is(bus.getName()));
        assertThat(responseBusDTOS.get(1).getId(), both(not(bus.getId())).and(is(bus1.getId())));
        assertThat(responseBusDTOS.get(1).getName(), is(bus1.getName()));
    }

    @Test
    public void official_agency_get_buses_should_throw_resource_not_found_exception_no_agency(){
        UserDTO userDTO = new UserDTO();
        User user = new User();
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("No Official Agency found for this user");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        carService.getAllOfficialAgencyBuses();
    }

    @Test
    public void official_agency_get_buses_should_throw_resource_not_found_exception_empty_agency(){
        UserDTO userDTO = new UserDTO();
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Agency is Empty");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        carService.getAllOfficialAgencyBuses();
    }

    @Test
    public void user_should_get_all_sharedRides(){
        SharedRide sharedRide = new SharedRide();
        sharedRide.setId(1L);
        sharedRide.setName("Happy");
        SharedRide sharedRide1 = new SharedRide();
        sharedRide1.setId(2L);
        sharedRide1.setName("Angry");
        UserDTO userDTO = new UserDTO();
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockPersonalAgency.getSharedRides()).thenReturn(new ArrayList<>(Arrays.asList(sharedRide, sharedRide1)));
        List<ResponseSharedRideDTO> responseSharedRideDTOS = carService.getAllSharedRides();
        assertThat(responseSharedRideDTOS.get(0).getId(), is(equalTo(sharedRide.getId())));
        assertThat(responseSharedRideDTOS.get(1).getName(), is(both(not(equalTo(sharedRide.getName())))
                .and(is(equalTo(sharedRide1.getName())))));
    }
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

}
