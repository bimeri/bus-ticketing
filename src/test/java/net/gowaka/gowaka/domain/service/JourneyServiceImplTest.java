package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.*;
import net.gowaka.gowaka.domain.repository.JourneyRepository;
import net.gowaka.gowaka.domain.repository.TransitAndStopRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.service.JourneyService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class JourneyServiceImplTest {
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

    private JourneyService journeyService;

    @Mock
    private OfficialAgency mockOfficialAgency;
    @Mock
    private PersonalAgency mockPersonalAgency;


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() throws Exception{
        journeyService = new JourneyServiceImpl(mockUserService, mockUserRepository, mockTransitAndStopRepository, mockJourneyRepository);
    }

    @Test
    public void add_journey_should_throw_car_resource_not_found_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Car not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addJourney(new JourneyDTO(), 1L);
    }

    @Test
    public void add_location_should_throw_transit_and_stop_not_found_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Township king");
        Bus bus1 = new Bus();
        bus1.setId(2L);
        bus1.setName("Contriman saga");

        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockOfficialAgency.getBuses()).thenReturn(new ArrayList<>(Arrays.asList(bus, bus1)));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addJourney(new JourneyDTO(), 1L);
    }

    @Test
    public void add_location_should_add_and_return_journey_response_dto(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Township king");
        Bus bus1 = new Bus();
        bus1.setId(2L);
        bus1.setName("Contriman saga");

        TransitAndStop transitAndStop = new TransitAndStop();
        Location location = new Location();
        location.setCity("Babanki");
        transitAndStop.setLocation(location);
        transitAndStop.setId(2L);
        JourneyDTO journeyDTO = new JourneyDTO();
        journeyDTO.setDepartureLocation(2L);
        journeyDTO.setDepartureTime(Date.from(
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        AddStopDTO addStopDTO = new AddStopDTO();
        addStopDTO.setTransitAndStopId(2L);
        addStopDTO.setAmount(1000);
        journeyDTO.setDestination(addStopDTO);
        journeyDTO.setTransitAndStops(Collections.singletonList(addStopDTO));

        Journey journey = new Journey();
        journey.setId(3L);


        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockOfficialAgency.getBuses()).thenReturn(new ArrayList<>(Arrays.asList(bus, bus1)));
        when(mockTransitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        when(mockJourneyRepository.save(any(Journey.class))).thenReturn(journey);

        JourneyResponseDTO journeyResponseDTO = journeyService.addJourney(journeyDTO, 1L);
        verify(mockTransitAndStopRepository, times(3)).findById(2L);
        assertThat(journeyResponseDTO.getArrivalIndicator(), is(false));
        assertThat(journeyResponseDTO.getDepartureIndicator(), is(false));
        assertThat(journeyResponseDTO.getDepartureTime(), is(journeyDTO.getDepartureTime()));
        assertThat(journeyResponseDTO.getDepartureLocation().getId(), is(transitAndStop.getId()));
        assertThat(journeyResponseDTO.getTransitAndStops().get(0).getId(), is(transitAndStop.getId()));
        assertThat(journeyResponseDTO.getDestination().getCity(), is(location.getCity()));
    }

    /**
     * Scenario 1 journeyId does not exist
     */
    @Test
    public void update_location_should_throw_journey_not_found_api_exception(){
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateJourney(new JourneyDTO(), 1L, 1L);
    }

    /**
     * scenario 2. carId not found in user's Official agency
     */
    @Test
    public void update_location_should_throw_car_not_found_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");

        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.emptyList());
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(new Journey()));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Car not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateJourney(new JourneyDTO(), 1L, 1L);
    }

    /**
     * #169112516
     * scenario 1 No journey found for user's Agency
     */
    @Test
    public void get_all_journeys_should_return_empty_list_if_no_journey_found(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");

        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findAllByOrderByTimestampDescArrivalIndicatorAsc())
                .thenReturn(Collections.emptyList());
        List<JourneyResponseDTO> journeyResponseDTOList = journeyService.getAllOfficialAgencyJourneys();
        assertTrue(journeyResponseDTOList.isEmpty());
    }

    /**
     * #169112516
     * scenario 2 Journeys found in user's Agency
     */
    @Test
    public void get_all_journeys_should_return_list_of_journey_response_dtos(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        bus.setOfficialAgency(mockOfficialAgency);

        Journey journey = new Journey();
        journey.setCar(bus);


        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findAllByOrderByTimestampDescArrivalIndicatorAsc())
                .thenReturn(Collections.singletonList(journey));
        when(mockTransitAndStopRepository.findDistinctFirstByLocation(any())).thenReturn(Optional.of(new TransitAndStop()));
        List<JourneyResponseDTO> journeyResponseDTOList = journeyService.getAllOfficialAgencyJourneys();
        assertFalse(journeyResponseDTOList.isEmpty());
        assertThat(journeyResponseDTOList.get(0).getCar(), is(instanceOf(CarResponseDTO.class)));
        assertThat(journeyResponseDTOList.get(0).getCar().getName(), is(bus.getName()));
    }

    /**
     * #169114688
     * Scenario 1. Journey not exist
     */
    @Test
    public void get_journey_by_id_should_throw_journey_not_found_api_exception(){
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.getJourneyById(1L);
    }

    /**
     * #169114688
     * Scenario 2. Journey' car not in authUser's agency
     */
    @Test
    public void get_journey_by_id_should_throw_car_not_found_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        bus.setOfficialAgency(mockOfficialAgency);

        Journey journey = new Journey();
        journey.setCar(bus);

        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(new Bus()));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s car not in AuthUser\'s Agency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.getJourneyById(1L);
    }

    /**
     * #169114688
     * Scenario 3. Journey Success
     */
    @Test
    public void get_journey_by_id_should_return_journey_response_dto(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        bus.setOfficialAgency(mockOfficialAgency);

        Journey journey = new Journey();
        journey.setCar(bus);

        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setId(2L);

        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockTransitAndStopRepository.findDistinctFirstByLocation(any())).thenReturn(Optional.of(transitAndStop));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(bus));
        journeyService.getJourneyById(1L);
    }

    /**
     * #169112805
     * Scenario 1. Journey's arrivalIndicator is true
     */
    @Test
    public void add_stops_should_throw_journey_already_terminated_api_exception(){
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey already terminated");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString())));
        journeyService.addStop(1L, new AddStopDTO());
    }

    /**
     * #169112805
     * Scenario 2. Journey not found
     */
    @Test
    public void add_stops_should_throw_journey_not_found_api_exception(){
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addStop(1L, new AddStopDTO());
    }

    /**
     * #169112805
     * Scenario 3. Journey not in AuthUser Agency
     */
    @Test
    public void add_stops_should_throw_journey_not_in_agency_api_exception() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        Bus bus1 = new Bus();
        bus1.setId(2L);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);


        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(bus1));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s car not in AuthUser\'s Agency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addStop(1L, new AddStopDTO());
    }

    /**
     * #169112805
     * Scenario 4. Transit and Stop not found
     */
    @Test
    public void add_stops_should_throw_transit_and_stop_not_found_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);


        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(bus));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addStop(1L, new AddStopDTO());
    }

    /**
     * #169112805
     * Scenario 5. Success
     */
    @Test
    public void add_stops_should_add_and_save_journey(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);

        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setId(2L);
        AddStopDTO addStopDTO = new AddStopDTO();
        addStopDTO.setTransitAndStopId(3L);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(bus));
        when(mockJourneyRepository.save(journey)).thenReturn(journey);
        when(mockTransitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        journeyService.addStop(1L, addStopDTO);
        verify(mockJourneyRepository).save(journey);
    }

    /**
     * #169112562
     * Scenario: 1 Journey not exist
     */
    @Test
    public void delete_journey_should_throw_journey_not_exist_api_exception(){
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.deleteNonBookedJourney(1L);
    }

    /**
     * #169112562
     * Scenario: 2 Journey's arrival indicator is true
     */
    @Test
    public void delete_journey_should_throw_journey_already_terminated_api_exception(){
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey already terminated");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString())));
        journeyService.deleteNonBookedJourney(1L);
    }

    /**
     * #169112562
     * Scenario: 3 Journey's car is not in AuthUser Agency
     */
    @Test
    public void delete_journey_should_throw_journey_not_in_auth_user_agency_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        Bus bus1 = new Bus();
        bus1.setId(2L);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);


        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(bus1));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s car not in AuthUser\'s Agency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.deleteNonBookedJourney(1L);
    }

    /**
     * #169114980
     * Scenario: 1. Journey not exist
     */
    @Test
    public void set_journey_departure_indicator_should_throw_journey_not_exist_api_exception(){
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO());
    }

    /**
     * #169114980
     * Scenario: 2. Journey already terminated
     */
    @Test
    public void set_journey_departure_indicator_should_throw_journey_already_terminated_api_exception(){
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey already terminated");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString())));
        journeyService.updateJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO());
    }

    /**
     * #169114980
     * Scenario: 3 Journey's car is not in AuthUser Agency
     */
    @Test
    public void set_journey_departure_indicator_should_throw_journey_not_in_auth_user_agency_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        Bus bus1 = new Bus();
        bus1.setId(2L);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(bus1));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s car not in AuthUser\'s Agency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateJourneyDepartureIndicator(4L, new JourneyDepartureIndicatorDTO());
    }

    /**
     * #169114979
     * Scenario: 1. Journey not exist
     */
    @Test
    public void update_journey_arrival_indicator_should_throw_journey_not_exist_api_exception(){
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO());
    }

    /**
     * #169114979
     * Scenario: 2. Journey not started
     */
    @Test
    public void update_journey_departure_indicator_should_throw_journey_not_started_api_exception(){
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not started");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.JOURNEY_NOT_STARTED.toString())));
        journeyService.updateJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO());
    }

    /**
     * #169114979
     * Scenario: 3 Journey's car is not in AuthUser Agency
     */
    @Test
    public void update_journey_arrival_indicator_should_throw_journey_not_in_auth_user_agency_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        Bus bus1 = new Bus();
        bus1.setId(2L);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        journey.setCar(bus);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(bus1));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s car not in AuthUser\'s Agency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateJourneyArrivalIndicator(4L, new JourneyArrivalIndicatorDTO());
    }

    /**
     * **USERS** Should be able to SharedRide Journey
     * #169527991
     * scenario: 1 carId not in user's personal agency
     */
    @Test
    public void personal_agency_add_journey_shared_rides_should_throw_not_found_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s Car not in AuthUser\'s PersonalAgency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addSharedJourney(new JourneyDTO(), 1L);
    }

    /**
     * **USERS** Should be able to SharedRide Journey
     * #169527991
     * scenario: 3 transitAndStopId don't exist
     */
    @Test
    public void personal_agency_add_journey_shared_rides_should_throw_transit_and_stop_not_found_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setId(1L);
        when(mockPersonalAgency.getSharedRides()).thenReturn(Collections.singletonList(sharedRide));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Destination TransitAndStop not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addSharedJourney(new JourneyDTO(), 1L);
    }
}
