package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.domain.model.Bus;
import net.gogroups.gowaka.domain.model.Car;
import net.gogroups.gowaka.domain.model.SharedRide;
import net.gogroups.gowaka.domain.repository.CarRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.CarService;
import net.gogroups.gowaka.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@ExtendWith(MockitoExtension.class)
public class CarControllerTest {
    @Mock
    private CarService mockCarService;

    private CarController carController;

    @BeforeEach
    void setup() {
        carController = new CarController(mockCarService);
    }

    @Test
    void official_agency_should_call_car_service_add_agency_bus_method() {
        BusDTO busDTO = new BusDTO();
        carController.addAgencyBus(busDTO);
        verify(mockCarService).addOfficialAgencyBus(busDTO);
    }

    @Test
    void official_agency_should_return_200_ok_status_code_with_responseBusDTO() {
        BusDTO busDTO = new BusDTO();
        busDTO.setName("Malingo Royal");
        busDTO.setNumberOfSeats(5);
        busDTO.setLicensePlateNumber("1234LT");

        when(mockCarService.addOfficialAgencyBus(busDTO)).thenAnswer(new Answer<BusResponseDTO>() {
            public BusResponseDTO answer(InvocationOnMock invocation) throws Throwable {
                BusResponseDTO busResponseDTO = new BusResponseDTO();
                busResponseDTO.setName(busDTO.getName());
                busResponseDTO.setLicensePlateNumber(busDTO.getLicensePlateNumber());
                busResponseDTO.setNumberOfSeats(busDTO.getNumberOfSeats());
                busResponseDTO.setId(1L);
                return busResponseDTO;
            }
        });
        ReflectionTestUtils.setField(carController, "carService", mockCarService);
        ResponseEntity<BusResponseDTO> response = carController.addAgencyBus(busDTO);
        verify(mockCarService).addOfficialAgencyBus(busDTO);
        BusResponseDTO responseBody = response.getBody();
        assertThat(responseBody.getId(), is(equalTo(1L)));
        assertThat(responseBody.getName(), is(equalTo(busDTO.getName())));
        assertThat(responseBody.getNumberOfSeats(), is(equalTo(busDTO.getNumberOfSeats())));
        assertThat(responseBody.getLicensePlateNumber(), is(equalTo(busDTO.getLicensePlateNumber())));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
    }

    @Test
    void personal_agency_should_call_car_service_add_sharedRide_method() {
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        carController.addSharedRide(sharedRideDTO);
        verify(mockCarService).addSharedRide(sharedRideDTO);
    }

    @Test
    void personal_agency_should_return_200_ok_status_code_with_responseSharedRideDTO() {
        SharedRideDTO sharedRideDTO = new SharedRideDTO();
        sharedRideDTO.setCarOwnerIdNumber("12345");
        sharedRideDTO.setName("Danfo Driver");
        sharedRideDTO.setCarOwnerName("Ndifor Fuh");
        sharedRideDTO.setLicensePlateNumber("1245NW");
        when(mockCarService.addSharedRide(sharedRideDTO)).thenAnswer(new Answer<SharedRideResponseDTO>() {
            public SharedRideResponseDTO answer(InvocationOnMock invocation) throws Throwable {
                SharedRideResponseDTO sharedRideResponseDTO = new SharedRideResponseDTO();
                sharedRideResponseDTO.setCarOwnerName(sharedRideDTO.getCarOwnerName());
                sharedRideResponseDTO.setCarOwnerIdNumber(sharedRideDTO.getCarOwnerIdNumber());
                sharedRideResponseDTO.setLicensePlateNumber(sharedRideDTO.getLicensePlateNumber());
                sharedRideResponseDTO.setName(sharedRideDTO.getName());
                sharedRideResponseDTO.setId(1L);
                return sharedRideResponseDTO;
            }
        });
        ReflectionTestUtils.setField(carController, "carService", mockCarService);
        ResponseEntity<SharedRideResponseDTO> response = carController.addSharedRide(sharedRideDTO);
        verify(mockCarService).addSharedRide(sharedRideDTO);
        SharedRideResponseDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertThat(responseBody.getName(), is(equalTo(sharedRideDTO.getName())));
        assertThat(responseBody.getCarOwnerIdNumber(), is(equalTo(sharedRideDTO.getCarOwnerIdNumber())));
        assertThat(responseBody.getLicensePlateNumber(), is(equalTo(sharedRideDTO.getLicensePlateNumber())));
        assertThat(responseBody.getCarOwnerName(), is(equalTo(sharedRideDTO.getCarOwnerName())));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
    }

    @Test
    void official_agency_get_all_buses_should_call_car_service_get_all_buses() {
        carController.getAllOfficialAgencyBuses();
        verify(mockCarService).getAllOfficialAgencyBuses();
    }

    @Test
    void official_agency_get_buses_should_call_car_service_get_all_buses() {
        ResponseEntity<BusResponseDTO> response = carController.getOfficialAgencyBuses(1L);
        verify(mockCarService).getOfficialAgencyBuses(1L);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void official_agency_get_all_buses_should_return_200_ok_with_responseBusDTO_list() {
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Happy");
        Bus bus1 = new Bus();
        bus1.setId(2L);
        bus1.setName("Angry");
        when(mockCarService.getAllOfficialAgencyBuses()).thenAnswer(new Answer<List<BusResponseDTO>>() {
            public List<BusResponseDTO> answer(InvocationOnMock invocation) throws Throwable {
                BusResponseDTO busResponseDTO = new BusResponseDTO();
                busResponseDTO.setId(bus.getId());
                busResponseDTO.setName(bus.getName());
                BusResponseDTO busResponseDTO1 = new BusResponseDTO();
                busResponseDTO1.setId(bus1.getId());
                busResponseDTO1.setName(bus1.getName());
                return new ArrayList<>(Arrays.asList(busResponseDTO, busResponseDTO1));
            }
        });
        ReflectionTestUtils.setField(carController, "carService", mockCarService);
        ResponseEntity<List<BusResponseDTO>> response = carController.getAllOfficialAgencyBuses();
        verify(mockCarService).getAllOfficialAgencyBuses();
        List<BusResponseDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertThat(responseBody.get(1).getId(), both(is(not(bus.getId()))).and(is(bus1.getId())));
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.OK)));
    }

    @Test
    void personal_agency_get_shareRides_should_call_car_service_getShareRides() {
        carController.getSharedRides();
        verify(mockCarService).getAllSharedRides();
    }

    @Test
    void personal_agency_get_shared_rides_should_return_200_with_response_shared_rides_list() {
        SharedRide sharedRide = new SharedRide();
        sharedRide.setId(1L);
        sharedRide.setName("OK");
        SharedRide sharedRide1 = new SharedRide();
        sharedRide1.setId(2L);
        sharedRide1.setName("UH");
        when(mockCarService.getAllSharedRides()).thenAnswer(new Answer<List<SharedRideResponseDTO>>() {
            public List<SharedRideResponseDTO> answer(InvocationOnMock invocation) throws Throwable {
                SharedRideResponseDTO sharedRideResponseDTO = new SharedRideResponseDTO();
                sharedRideResponseDTO.setId(sharedRide.getId());
                sharedRideResponseDTO.setName(sharedRide.getName());
                SharedRideResponseDTO sharedRideResponseDTO1 = new SharedRideResponseDTO();
                sharedRideResponseDTO1.setId(sharedRide1.getId());
                sharedRideResponseDTO1.setName(sharedRide1.getName());
                return new ArrayList<>(Arrays.asList(sharedRideResponseDTO, sharedRideResponseDTO1));
            }
        });
        ReflectionTestUtils.setField(carController, "carService", mockCarService);
        ResponseEntity<List<SharedRideResponseDTO>> responseEntity = carController.getSharedRides();
        assertThat(responseEntity.getStatusCode(), is(equalTo(HttpStatus.OK)));
        List<SharedRideResponseDTO> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertThat(responseBody.get(1).getName(), is(both(not(equalTo(sharedRide.getName())))
                .and(equalTo(sharedRide1.getName()))));
    }

    @Test
    void approve_should_call_car_service_approve_method() {
        ApproveCarDTO approveCarDTO = new ApproveCarDTO();
        carController.shallApprove(approveCarDTO, "1");
        verify(mockCarService).approve(approveCarDTO, 1L);
    }

    @Test
    void approve_should_return_204_no_content_status_code() {
        Car car = new Bus();
        ApproveCarDTO approveCarDTO = new ApproveCarDTO();
        car.setId(1L);

        approveCarDTO.setApprove(true);
        ResponseEntity<Object> responseEntity = carController.shallApprove(approveCarDTO, "1");

        assertThat(responseEntity.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));

        approveCarDTO.setApprove(false);
        carController.shallApprove(approveCarDTO, "1");

        assertThat(responseEntity.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
    }

    @Test
    void getAllUnapprovedCars_calls_carService() {
        carController.getAllUnapprovedCars();
        verify(mockCarService).getAllUnapprovedCars();
    }

    @Test
    void getSeatStructures_returnsListOfSeatStructure() {
        when(mockCarService.getSeatStructures("CODE"))
                .thenReturn(Collections.singletonList(new SeatStructureDTO()));

        ResponseEntity<List<SeatStructureDTO>> seatStructures = carController.getSeatStructures("CODE");
        verify(mockCarService).getSeatStructures("CODE");
        assertThat(seatStructures.getBody().size(), is(1));
    }
}
