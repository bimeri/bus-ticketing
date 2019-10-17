package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.JourneyRepository;
import net.gowaka.gowaka.domain.repository.TransitAndStopRepository;
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
     @Mock
     private TransitAndStopRepository mockTransitAndStopRepository;
     @Mock
     private JourneyRepository mockJourneyRepository;

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
        List<BusResponseDTO> busResponseDTOS = carService.getAllOfficialAgencyBuses();
        assertThat(busResponseDTOS.get(0).getId(), is(bus.getId()));
        assertThat(busResponseDTOS.get(0).getName(), is(bus.getName()));
        assertThat(busResponseDTOS.get(1).getId(), both(not(bus.getId())).and(is(bus1.getId())));
        assertThat(busResponseDTOS.get(1).getName(), is(bus1.getName()));
    }

    @Test
    public void official_agency_get_buses_should_throw_resource_not_found_exception_no_agency(){
        UserDTO userDTO = new UserDTO();
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
        List<SharedRideResponseDTO> sharedRideResponseDTOS = carService.getAllSharedRides();
        assertThat(sharedRideResponseDTOS.get(0).getId(), is(equalTo(sharedRide.getId())));
        assertThat(sharedRideResponseDTOS.get(1).getName(), is(both(not(equalTo(sharedRide.getName())))
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

     @Test
     public void should_throw_car_not_found_api_exception(){
         Car car = new Bus();
         ApproveCarDTO approveCarDTO = new ApproveCarDTO();
         car.setId(1L);
         approveCarDTO.setApprove(true);
         expectedException.expect(ApiException.class);
         expectedException.expectMessage("Car not found");
         expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
         carService.approve(approveCarDTO, 1L);
     }

     @Test
     public void should_throw_license_plate_number_in_use_api_exception(){
         SharedRide sharedRide = new SharedRide();
         sharedRide.setId(1L);
         sharedRide.setLicensePlateNumber("12345");
         user.setUserId("1");
         SharedRideDTO sharedRideDTO = new SharedRideDTO();
         sharedRideDTO.setLicensePlateNumber("12345");
         UserDTO userDTO = new UserDTO();
         userDTO.setId("1");
         when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
         when(mockCarRepository.findByLicensePlateNumberIgnoreCase(anyString())).thenReturn(Optional.of(sharedRide));
         when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
         when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
         expectedException.expect(ApiException.class);
         expectedException.expectMessage("License plate number already in use");
         expectedException.expect(hasProperty("errorCode", is(ErrorCodes.LICENSE_PLATE_NUMBER_ALREADY_IN_USE.toString())));
         carService.addSharedRide(sharedRideDTO);
     }

     @Test
     public void user_get_all_shared_rides_should_throw_agency_not_found_api_exception(){
         SharedRide sharedRide = new SharedRide();
         sharedRide.setId(1L);
         sharedRide.setLicensePlateNumber("12345");
         user.setUserId("1");
         SharedRideDTO sharedRideDTO = new SharedRideDTO();
         sharedRideDTO.setLicensePlateNumber("12345");
         UserDTO userDTO = new UserDTO();
         userDTO.setId("1");
         when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
         when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
         expectedException.expect(ApiException.class);
         expectedException.expectMessage("No Personal Agency found for this user");
         expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
         carService.getAllSharedRides();
     }

    @Test
    public void getAllUnapprovedCars_calls_CarRepository_for_all_unapproved_cars(){

        Car bus = new Bus();
        bus.setId(1L);
        bus.setIsCarApproved(false);
        bus.setIsOfficialAgencyIndicator(true);
        bus.setLicensePlateNumber("123LT");
        bus.setName("Kontri People");

        Car sharedRide = new SharedRide();
        sharedRide.setId(2L);
        sharedRide.setIsCarApproved(false);
        sharedRide.setIsOfficialAgencyIndicator(false);
        sharedRide.setLicensePlateNumber("321LT");
        sharedRide.setName("Jungle Boy");

        when(mockCarRepository.findByIsCarApproved(false))
                 .thenReturn(Arrays.asList(bus, sharedRide));

        List<CarDTO> allUnapprovedCars = carService.getAllUnapprovedCars();
        assertThat(allUnapprovedCars.size(), is(2));

        assertThat(allUnapprovedCars.get(0).getId(), is(1L));
        assertThat(allUnapprovedCars.get(0).getIsCarApproved(), is(false));
        assertThat(allUnapprovedCars.get(0).getIsOfficialAgencyIndicator(), is(true));
        assertThat(allUnapprovedCars.get(0).getLicensePlateNumber(), is("123LT"));
        assertThat(allUnapprovedCars.get(0).getName(), is("Kontri People"));

        assertThat(allUnapprovedCars.get(1).getId(), is(2L));
        assertThat(allUnapprovedCars.get(1).getIsCarApproved(), is(false));
        assertThat(allUnapprovedCars.get(1).getIsOfficialAgencyIndicator(), is(false));
        assertThat(allUnapprovedCars.get(1).getLicensePlateNumber(), is("321LT"));
        assertThat(allUnapprovedCars.get(1).getName(), is("Jungle Boy"));

    }

     @Test
     public void search_by_license_plate_should_throw_car_not_found_api_exception(){
         expectedException.expect(ApiException.class);
         expectedException.expectMessage("Car not found");
         expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
         carService.searchByLicensePlateNumber("123");
     }

     @Test
     public void search_by_license_plate_should_return_response_car_dto(){
         Car car = new SharedRide();
         car.setId(1L);
         car.setLicensePlateNumber("1234LT");
         when(mockCarRepository.findByLicensePlateNumberIgnoreCase(anyString())).thenReturn(Optional.of(car));
         assertThat(carService.searchByLicensePlateNumber(car.getLicensePlateNumber()).getId(), is(equalTo(car.getId())));
     }

}
