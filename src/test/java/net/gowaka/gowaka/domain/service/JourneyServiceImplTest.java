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


import java.time.*;
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

    /*@Spy
    private LocalDateTime spyMockLocalDateTime;*/


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

        journey.setCar(bus);
        journey.setDepartureIndicator(false);
        journey.setArrivalIndicator(false);
        JourneyStop journeyStop = new JourneyStop(journey, transitAndStop, 3500);

        journey.setJourneyStops(Collections.singleton(journeyStop));
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
        journey.setDepartureIndicator(false);
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
        journey.setDepartureIndicator(true);
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

    /**
     * **USERS** can update information about  Journey for PersonalAgency
     * #169528238
     * scenario: 1 Journey does not exist
     */
    @Test
    public void personal_agency_update_journey_shared_rides_should_throw_journey_not_found_api_exception(){
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateSharedJourney(new JourneyDTO(), 1L, 1L);
    }

    /**
     * **USERS** can update information about  Journey for PersonalAgency
     * #169528238
     * scenario: 2 Car not in AuthUser's PersonalAgency
     */
    @Test
    public void personal_agency_update_journey_shared_rides_should_throw_car_not_found_api_exception(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(new Journey()));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s Car not in AuthUser\'s PersonalAgency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateSharedJourney(new JourneyDTO(), 1L, 1L);
    }

    /**
     * **USERS** can GET  a Journey for OfficialAgency
     * #169528470
     * Scenario: 1 Journeys Not exist
     */
    @Test
    public void personal_agency_get_journey_should_throw_journey_not_exist_api_exception(){
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.getSharedJourneyById(1L);
    }

    /**
     * **USERS** can GET  a Journey for OfficialAgency
     * #169528470
     * Scenario: 2 Journey's Car not in AuthUser's personalAgency
     */
    @Test
    public void personal_agency_get_journey_should_throw_car_not_in_personal_agency_api_exception(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(new Journey()));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s Car not in AuthUser\'s PersonalAgency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.getSharedJourneyById(1L);
    }

    /**
     * **USERS**  can view all Journey for PersonalAgency ordered by date and arrivalIndicator
     * #169528531
     * Scenario: 1. No Journey found for user's Agency
     *
     */
    @Test
    public void Given_No_journey_exist_for_that_user_personal_agency(){

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Journey journey = new Journey();
        journey.setId(1L);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setId(1L);
        sharedRide.setPersonalAgency(new PersonalAgency());
        journey.setCar(sharedRide);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        when(mockJourneyRepository.findAllByOrderByTimestampDescArrivalIndicatorAsc()).thenReturn(Collections.singletonList(journey));
        List<JourneyResponseDTO> journeyList = journeyService.getAllPersonalAgencyJourneys();
        assertTrue(journeyList.isEmpty());
    }

    /**
     * USERS can change departureIndicator state
     *#169528573
     *Scenario:  1. Journeys NOT exist
     */
    @Test
    public void Journeys_NOT_exist_Given_JourneyId_passed_as_parameter_NOT_exit(){
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateSharedJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO());
    }


    /**
     * USERS can change departureIndicator state
     *#169528573
     *Scenario:  3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    public void Journey_car_is_NOT_in_AuthUser_Agency_Given_journeyId_passed_and_arrivalIndicator_false(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s Car not in AuthUser\'s PersonalAgency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateSharedJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO());
    }

    /**
     * USERS can change departureIndicator state
     * #169528573
     * Scenario:  2. Journey's arrivalIndicator is true;
     */
    @Test
    public void given_journeyId_passed_as_parameter_exist_and_journey_arrivalIndicator_true_throw_journey_terminated_exception(){
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey already terminated");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString())));
        journeyService.updateSharedJourneyDepartureIndicator(journey.getId(), new JourneyDepartureIndicatorDTO());
    }
    /**
     * USERS can change departureIndicator state
     * #169528573
     * Scenario:  4. change  Journey departureIndicator state
     */
    @Test
    public  void  given_journeyId_passed_as_parameter_exist_and_journey_arrivalIndicator_false_and_Journey_car_is_IN_AuthUser_Agency_then_change_departure_state(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setPersonalAgency(mockPersonalAgency);
        sharedRide.setId(1L);
        journey.setCar(sharedRide);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockJourneyRepository.save(any(Journey.class))).thenReturn(journey);
        when(mockPersonalAgency.getSharedRides()).thenReturn(Collections.singletonList(sharedRide));
        journeyService.updateSharedJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO());
        verify(mockJourneyRepository).save(journey);
    }

    /**
     * **USERS** can change arrivalIndicator stat
     * #169528624
     * Scenario:  1. Journeys NOT exist
     */
    @Test
    public void Given_JourneyId_passed_as_parameter_Journeys_NOT_exis(){

        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect( hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateSharedJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO());
    }

    /**
     * **USERS** can change arrivalIndicator stat
     * #169528624
     * Scenario:  2. Journey's departure is false;
     */
    @Test
    public void journeyId_passed_as_parameter_exist_and_Journey_departure_Indicator_false(){
        Journey journey = new Journey();
        journey.setDepartureIndicator(false);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not started");
        expectedException.expect(hasProperty("errorCode",is(ErrorCodes.JOURNEY_NOT_STARTED.toString())));
        journeyService.updateSharedJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO());
    }

    /**
     * **USERS** can change arrivalIndicator stat
     * #169528624
     * Scenario:  3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    public void journeyId_as_parameter_exist_and_journey_departureIndicator_true_journey_car_not_in_authUser_agency(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Journey journey = new Journey();
        journey.setDepartureIndicator(true);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s Car not in AuthUser\'s PersonalAgency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.updateSharedJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO());
    }
    /**
     * **USERS** can change arrivalIndicator stat
     * #169528624
     * Scenario:  4. change  Journey arrivalIndicator state
     */
    @Test
    public void journeyId_as_parameter_exist_and_journey_departureIndicator_true_journey_car_is_in_authUser_agency(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(true);
        journey.setArrivalIndicator(false);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setPersonalAgency(mockPersonalAgency);
        sharedRide.setId(1L);
        journey.setCar(sharedRide);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockJourneyRepository.save(any(Journey.class))).thenReturn(journey);
        when(mockPersonalAgency.getSharedRides()).thenReturn(Collections.singletonList(sharedRide));
        JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO = new JourneyArrivalIndicatorDTO();
        journeyArrivalIndicatorDTO.setArrivalIndicator(true);
        journeyService.updateSharedJourneyArrivalIndicator(1L, journeyArrivalIndicatorDTO);
        verify(mockJourneyRepository).save(journey);
    }
    /**
     * **USERS** can delete  Journey for PersonalAgency  if NO booking and arrivalIndicator = false
     * #169528640
     * Scenario:  1. Journeys NOT exist
     */
    @Test
    public void given_JourneyId_passed_as_parameter_not_exit(){
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect( hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.deleteNonBookedSharedJourney(1L);
    }
    /**
     * **USERS** can delete  Journey for PersonalAgency  if NO booking and arrivalIndicator = false
     * #169528640
     *Scenario:  2. Journey's arrivalIndicator is truet
     */
    @Test
    public void given_journeyId_passed_as_parameter_exit_and_journey_arrival_indicator_true_journey_already_terminated(){
        Journey journey = new Journey();
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey already terminated");
        expectedException.expect( hasProperty("errorCode", is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString())));
        journeyService.deleteNonBookedSharedJourney(1L);
    }
    /**
     * **USERS** can delete  Journey for PersonalAgency  if NO booking and arrivalIndicator = false
     * #169528640
     *Scenario:  3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    public void given_journeyId_passed_as_parameter_exist_and_journey_arrivalIndicator_false_but_journey_car_is_not_in_authUser_agency(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Journey journey = new Journey();
        journey.setArrivalIndicator(false);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s Car not in AuthUser\'s PersonalAgency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.deleteNonBookedSharedJourney(1L);
    }
    /**
     * **USERS** can delete  Journey for PersonalAgency  if NO booking and arrivalIndicator = false
     * #169528640
     *Scenario:  4. Delete Journey Success
     */
    @Test
    public void delete_shared_journey_should_call_journey_repository_delete(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setPersonalAgency(mockPersonalAgency);
        sharedRide.setId(1L);
        journey.setCar(sharedRide);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        when(mockPersonalAgency.getSharedRides()).thenReturn(Collections.singletonList(sharedRide));
        journeyService.deleteNonBookedSharedJourney(journey.getId());
        verify(mockJourneyRepository).delete(journey);
    }

    /**
     * **USERS** can add Journey STOPS  Journey for PersonalAgency  if  arrivalIndicator = false
     * #169528838
     * Scenario:  1. Journey's arrivalIndicator is true
     */
    @Test
    public void throw_journey_already_exist_given_arrivalIndicator_is_true_with_id(){
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey already terminated");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString())));
        journeyService.addStopToPersonalAgency(1L, new AddStopDTO());
    }

    /**
     * **USERS** can add Journey STOPS  Journey for PersonalAgency  if  arrivalIndicator = false
     * #169528838
     * Scenario:  2. Journeys NOT exist
     */
    @Test
    public void throw_error_resource_not_found_given_journey_does_not_exist(){
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addStopToPersonalAgency(1L, new AddStopDTO());

    }

    /**
     * **USERS** can add Journey STOPS  Journey for PersonalAgency  if  arrivalIndicator = false
     * #169528838
     * Scenario:  3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    public void throw_resource_not_found_given_arrivalIndicator_is_false_with_id_car_is_not_in_autUser_agency(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Journey\'s Car not in AuthUser\'s PersonalAgency");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addStopToPersonalAgency(1L, new AddStopDTO());
    }

    /**
     * **USERS** can add Journey STOPS  Journey for PersonalAgency  if  arrivalIndicator = false
     * #169528838
     * Scenario:  4. TransitAndStopId don't exit
     */

    @Test
    public void throw_transit_and_stop_not_found_given_transit_and_stop_false_car_not_in_authUser_agency(){
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        SharedRide sharedRide = new SharedRide();
        sharedRide.setId(1L);

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        journey.setCar(sharedRide);


        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        when(mockPersonalAgency.getSharedRides()).thenReturn(Collections.singletonList(sharedRide));

        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.addStopToPersonalAgency(1L, new AddStopDTO());
    }

    /**
     * **add Search Journey api endpoint
     * #169528838
     * Scenario: 1. Wrong input data passed
     */
    @Test
    public void throw_bad_request_given_wrong_date_time_input_data_passed_when_during_search() {
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Invalid date time format. Try yyyy-MM-dd HH:mm");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.INVALID_FORMAT.toString())));
        journeyService.searchJourney(1L, 1L,"9999999" );


    }

    /**
     * **add Search Journey api endpoint
     * #169528838
     * Scenario: 1. Wrong input data passed
     */
    @Test
    public void throw_bad_request_given_wrong_departure_id_input_data_passed_when_during_search() {
        when(mockTransitAndStopRepository.findById(1L)).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Departure TransitAndStop not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.searchJourney(1L, 1L,"2020-01-20 12:30" );


    }

    /**
     * **add Search Journey api endpoint
     * #169528838
     * Scenario: 1. Wrong input data passed
     */
    @Test
    public void throw_bad_request_given_wrong_destination_id_input_data_passed_when_during_search() {
        when(mockTransitAndStopRepository.findById(1L)).thenReturn(Optional.of(new TransitAndStop()));
        when(mockTransitAndStopRepository.findById(2L)).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Destination TransitAndStop not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        journeyService.searchJourney(1L, 2L,"2020-01-20 12:30" );

    }





}
