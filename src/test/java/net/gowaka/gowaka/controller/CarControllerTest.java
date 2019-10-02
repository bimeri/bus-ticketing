package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.domain.repository.CarRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.domain.service.CarServiceImpl;
import net.gowaka.gowaka.dto.BusDTO;
import net.gowaka.gowaka.dto.ResponseBusDTO;
import net.gowaka.gowaka.dto.ResponseSharedRideDTO;
import net.gowaka.gowaka.dto.SharedRideDTO;
import net.gowaka.gowaka.service.CarService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void official_agency_should_call_car_service_add_agency_bus_method(){
        BusDTO busDTO = new BusDTO();
        carController.addAgencyBus(busDTO);
        verify(mockCarService).addOfficialAgencyBus(busDTO);
    }

    @Test
    public void official_agency_should_return_200_ok_status_code_with_responseBusDTO(){
        BusDTO busDTO = new BusDTO();
        busDTO.setName("Malingo Royal");
        busDTO.setNumberOfSeats(5);
        busDTO.setLicensePlateNumber("1234LT");

        when(mockCarService.addOfficialAgencyBus(busDTO)).thenAnswer(new Answer<ResponseBusDTO>() {
            public ResponseBusDTO answer(InvocationOnMock invocation) throws Throwable {
                ResponseBusDTO responseBusDTO = new ResponseBusDTO();
                responseBusDTO.setName(busDTO.getName());
                responseBusDTO.setLicensePlateNumber(busDTO.getLicensePlateNumber());
                responseBusDTO.setNumberOfSeats(busDTO.getNumberOfSeats());
                responseBusDTO.setId(1L);
                return responseBusDTO;
            }
        });
        ReflectionTestUtils.setField(carController, "carService", mockCarService);
        ResponseEntity<ResponseBusDTO> response = carController.addAgencyBus(busDTO);
        verify(mockCarService).addOfficialAgencyBus(busDTO);
        ResponseBusDTO responseBody = response.getBody();
        assertThat(responseBody.getId(), is(equalTo(1L)));
        assertThat(responseBody.getName(), is(equalTo(busDTO.getName())));
        assertThat(responseBody.getNumberOfSeats(), is(equalTo(busDTO.getNumberOfSeats())));
        assertThat(responseBody.getLicensePlateNumber(), is(equalTo(busDTO.getLicensePlateNumber())));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
    }

    @Test
    public void personal_agency_should_call_car_service_add_sharedRide_method(){
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        carController.addSharedRide(sharedRideDTO);
        verify(mockCarService).addSharedRide(sharedRideDTO);
    }

    @Test
    public void personal_agency_should_return_200_ok_status_code_with_responseSharedRideDTO(){
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        sharedRideDTO.setCarOwnerIdNumber("12345");
        sharedRideDTO.setName("Danfo Driver");
        sharedRideDTO.setCarOwnerName("Ndifor Fuh");
        sharedRideDTO.setLicensePlateNumber("1245NW");
        when(mockCarService.addSharedRide(sharedRideDTO)).thenAnswer(new Answer<ResponseSharedRideDTO>() {
            public ResponseSharedRideDTO answer(InvocationOnMock invocation) throws Throwable {
                ResponseSharedRideDTO responseSharedRideDTO = new ResponseSharedRideDTO();
                responseSharedRideDTO.setCarOwnerName(sharedRideDTO.getCarOwnerName());
                responseSharedRideDTO.setCarOwnerIdNumber(sharedRideDTO.getCarOwnerIdNumber());
                responseSharedRideDTO.setLicensePlateNumber(sharedRideDTO.getLicensePlateNumber());
                responseSharedRideDTO.setName(sharedRideDTO.getName());
                responseSharedRideDTO.setId(1L);
                return responseSharedRideDTO;
            }
        });
        ReflectionTestUtils.setField(carController, "carService", mockCarService);
        ResponseEntity<ResponseSharedRideDTO> response = carController.addSharedRide(sharedRideDTO);
        verify(mockCarService).addSharedRide(sharedRideDTO);
        ResponseSharedRideDTO responseBody = response.getBody();
        assertThat(responseBody.getId(), is(equalTo(1L)));
        assertThat(responseBody.getName(), is(equalTo(sharedRideDTO.getName())));
        assertThat(responseBody.getCarOwnerIdNumber(), is(equalTo(sharedRideDTO.getCarOwnerIdNumber())));
        assertThat(responseBody.getLicensePlateNumber(), is(equalTo(sharedRideDTO.getLicensePlateNumber())));
        assertThat(responseBody.getCarOwnerName(), is(equalTo(sharedRideDTO.getCarOwnerName())));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
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
