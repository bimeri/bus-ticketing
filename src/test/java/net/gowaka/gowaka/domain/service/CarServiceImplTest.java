package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.Bus;
import net.gowaka.gowaka.domain.model.SharedRide;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.SharedRideDTO;
import net.gowaka.gowaka.dto.UserDTO;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.service.CarService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

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

     @Test(expected = ApiException.class)
     public void official_agency_should_throw_access_denied_exception(){
         BusDTO busDTO = new BusDTO();
         UserDTO userDTO = new UserDTO();
         when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
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

    @Test(expected = ApiException.class)
    public void private_agency_should_throw_access_denied_exception(){
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        UserDTO userDTO = new UserDTO();
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        carService.addSharedRide(sharedRideDTO);
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
