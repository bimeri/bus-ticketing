package net.gogroups.gowaka.domain.service;

import net.gogroups.cfs.service.CfsClientService;
import net.gogroups.dto.PaginatedResponse;
import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.*;
import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.GwCacheLoaderService;
import net.gogroups.gowaka.service.UserService;
import net.gogroups.notification.model.SendEmailDTO;
import net.gogroups.notification.model.SendSmsDTO;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.storage.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author Nnouka Stephen
 * @date 26 Sep 2019
 */
@ExtendWith(MockitoExtension.class)
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

    @Mock
    private JourneyStopRepository mockJourneyStopRepository;

    @Mock
    private BookedJourneyRepository mockBookedJourneyRepository;

    @InjectMocks
    private JourneyServiceImpl journeyService;

    @Mock
    private OfficialAgency mockOfficialAgency;
    @Mock
    private PersonalAgency mockPersonalAgency;

    @Mock
    private GgCfsSurveyTemplateJsonRepository mockGgCfsSurveyTemplateJsonRepository;

    @Mock
    private CfsClientService mockCfsClientService;

    @Mock
    private FileStorageService mockFileStorageService;

    @Mock
    private NotificationService mockNotificationService;

    @Mock
    private EmailContentBuilder mockEmailContentBuilder;

    @Mock
    private GwCacheLoaderService mockGwCacheLoaderService;


    @Test
    void add_journey_should_throw_car_resource_not_found_api_exception() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addJourney(new JourneyDTO(), 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Car not found."));

    }

    @Test
    void add_location_should_throw_transit_and_stop_not_found_api_exception() {
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

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addJourney(new JourneyDTO(), 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Destination TransitAndStop not found"));
    }

    @Test
    void add_location_should_add_and_return_journey_response_dto() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Township king");
        Bus bus1 = new Bus();
        bus1.setId(2L);
        bus1.setName("Contriman saga");
        SeatStructure seatStructure = new SeatStructure();
        seatStructure.setSeatStructureCode("CODE1");
        seatStructure.setNumberOfSeats(10);
        seatStructure.setImage("image");
        bus.setSeatStructure(seatStructure);

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
        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(2L);
        agencyBranch.setName("ACL");
        journey.setAgencyBranch(agencyBranch);

        journey.setJourneyStops(Collections.singletonList(journeyStop));
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
    void update_location_should_throw_journey_not_found_api_exception() {

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourney(new JourneyDTO(), 1L, 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * scenario 2. carId not found in user's Official agency
     */
    @Test
    void update_location_should_throw_car_not_found_api_exception() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");
        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(agencyBranch);
        OfficialAgency officialAgency = new OfficialAgency();
        user.setOfficialAgency(officialAgency);

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        Bus bus1 = new Bus();
        bus1.setId(2L);
        bus1.setName("Happi");
        Journey journey = new Journey();
        journey.setArrivalIndicator(false);
        journey.setCar(bus1);
        journey.setAgencyBranch(agencyBranch);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourney(new JourneyDTO(), 1L, 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Car not found."));
    }

    /**
     * #169112516
     * scenario 1 No journey found for user's Agency
     */
    @Test
    void get_all_journeys_should_return_empty_list_if_no_journey_found() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");

        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findAllByOrderByCreatedAtDescArrivalIndicatorAsc())
                .thenReturn(Collections.emptyList());
        List<JourneyResponseDTO> journeyResponseDTOList = journeyService.getAllOfficialAgencyJourneys();
        assertTrue(journeyResponseDTOList.isEmpty());
    }

    @Test
    void getOfficialAgencyJourneys_should_return_list_of_journeys() {

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        bus.setOfficialAgency(mockOfficialAgency);
        SeatStructure seatStructure = new SeatStructure();
        seatStructure.setSeatStructureCode("CODE1");
        seatStructure.setNumberOfSeats(10);
        seatStructure.setImage("image");
        bus.setSeatStructure(seatStructure);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setBuses(Collections.singletonList(bus));

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setOfficialAgency(officialAgency);
        agencyBranch.setId(1L);
        Journey journey = new Journey();
        officialAgency.setAgencyBranch(Collections.singletonList(agencyBranch));

        journey.setCar(bus);
        journey.setDepartureTime(LocalDateTime.MIN);
        journey.setAgencyBranch(agencyBranch);

        User user = new User();
        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(agencyBranch);
        when(mockUserRepository.findById("1"))
                .thenReturn(Optional.of(user));
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);

        when(mockJourneyRepository.findByAgencyBranch_IdOrderByCreatedAtDescArrivalIndicatorAsc(any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(journey)));

        PaginatedResponse<JourneyResponseDTO> officialAgencyJourneys = journeyService.getOfficialAgencyJourneys(1, 10, 1L);
        assertFalse(officialAgencyJourneys.getItems().isEmpty());
        assertThat(officialAgencyJourneys.getTotalPages(), is(1));
        assertThat(officialAgencyJourneys.getPageNumber(), is(1));
        assertThat(officialAgencyJourneys.getTotal(), is(1));
        assertThat(officialAgencyJourneys.getItems().get(0).getCar().getName(), is("Muea boy"));
        assertThat(officialAgencyJourneys.getItems().get(0).isDepartureTimeDue(), is(true));
    }

    @Test
    void getOfficialAgencyJourneys_throwException_whenUserBranchNotFound() {

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        bus.setOfficialAgency(mockOfficialAgency);

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setBuses(Collections.singletonList(bus));

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setOfficialAgency(officialAgency);
        agencyBranch.setId(1L);
        Journey journey = new Journey();

        journey.setCar(bus);
        journey.setDepartureTime(LocalDateTime.MIN);
        journey.setAgencyBranch(agencyBranch);

        User user = new User();
        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(agencyBranch);
        when(mockUserRepository.findById("1"))
                .thenReturn(Optional.of(user));
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);

        ResourceNotFoundException apiException = assertThrows(ResourceNotFoundException.class, () -> journeyService.getOfficialAgencyJourneys(1, 10, 1L));
        assertThat(apiException.getMessage(), is("Branch not found in user's agency"));

    }

    /**
     * #169112516
     * scenario 2 Journeys found in user's Agency
     */
    @Test
    void get_all_journeys_should_return_list_of_journey_response_dtos() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        bus.setOfficialAgency(mockOfficialAgency);
        SeatStructure seatStructure = new SeatStructure();
        seatStructure.setSeatStructureCode("CODE1");
        seatStructure.setNumberOfSeats(10);
        seatStructure.setImage("image");
        bus.setSeatStructure(seatStructure);

        Journey journey = new Journey();
        journey.setCar(bus);
        journey.setDepartureTime(LocalDateTime.MIN);

        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findAllByOrderByCreatedAtDescArrivalIndicatorAsc())
                .thenReturn(Collections.singletonList(journey));
        List<JourneyResponseDTO> journeyResponseDTOList = journeyService.getAllOfficialAgencyJourneys();
        assertFalse(journeyResponseDTOList.isEmpty());
        assertThat(journeyResponseDTOList.get(0).getCar(), is(instanceOf(CarResponseDTO.class)));
        assertThat(journeyResponseDTOList.get(0).getCar().getName(), is(bus.getName()));
        assertThat(journeyResponseDTOList.get(0).isDepartureTimeDue(), is(true));
    }

    /**
     * #169114688
     * Scenario 1. Journey not exist
     */
    @Test
    void get_journey_by_id_should_throw_journey_not_found_api_exception() {
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.getJourneyById(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * #169114688
     * Scenario 2. Journey' car not in authUser's agency
     */
    @Test
    void get_journey_by_id_should_throw_car_not_found_api_exception() {
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

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.getJourneyById(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s car not in AuthUser\'s Agency"));

    }

    /**
     * #169114688
     * Scenario 3. Journey Success
     */
    @Test
    void get_journey_by_id_should_return_journey_response_dto() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        bus.setOfficialAgency(mockOfficialAgency);
        SeatStructure seatStructure = new SeatStructure();
        seatStructure.setSeatStructureCode("CODE1");
        seatStructure.setNumberOfSeats(10);
        seatStructure.setImage("image");
        bus.setSeatStructure(seatStructure);

        Journey journey = new Journey();
        journey.setCar(bus);
        journey.setDepartureTime(LocalDateTime.MIN);

        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setId(2L);

        when(user.getOfficialAgency()).thenReturn(mockOfficialAgency);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockOfficialAgency.getBuses()).thenReturn(Collections.singletonList(bus));
        journeyService.getJourneyById(1L);
    }


    @Test
    void getAJourneyById_throw_journey_not_found_api_exception() {
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.getJourneyById(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    @Test
    void getAJourneyById_return_journey_response_dto() {

        Journey journey = new Journey();
        journey.setAmount(1000.0);
        journey.setDepartureTime(LocalDateTime.MIN);

        when(mockJourneyRepository.findById(1L)).thenReturn(Optional.of(journey));
        JourneyResponseDTO journeyResponseDTO = journeyService.getAJourneyById(1L);
        assertThat(journeyResponseDTO.getAmount(), is(1000.0));

    }


    /**
     * #169112805
     * Scenario 1. Journey's arrivalIndicator is true
     */
    @Test
    void add_stops_should_throw_journey_already_terminated_api_exception() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addStop(1L, new AddStopDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString()));
        assertThat(apiException.getMessage(), is("Journey already terminated"));
    }

    /**
     * #169112805
     * Scenario 2. Journey not found
     */
    @Test
    void add_stops_should_throw_journey_not_found_api_exception() {
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addStop(1L, new AddStopDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * #169112805
     * Scenario 3. Journey not in AuthUser Agency
     */
    @Test
    void add_stops_should_throw_journey_not_in_agency_api_exception() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");
        User user = new User();
        user.setUserId("1");
        user.setOfficialAgency(new OfficialAgency());
        user.setAgencyBranch(new AgencyBranch());

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
        journey.setAgencyBranch(agencyBranch);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        ApiException apiException = assertThrows(ApiException.class, () -> {
            journeyService.addStop(1L, new AddStopDTO());
        });
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s car not in AuthUser\'s Agency"));
    }

    /**
     * #169112805
     * Scenario 4. Transit and Stop not found
     */
    @Test
    void add_stops_should_throw_transit_and_stop_not_found_api_exception() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");
        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(agencyBranch);

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);
        journey.setAgencyBranch(agencyBranch);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addStop(1L, new AddStopDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("TransitAndStop not found"));
    }

    /**
     * #169112805
     * Scenario 5. Success
     */
    @Test
    void add_stops_should_add_and_save_journey() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");
        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(agencyBranch);

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        journey.setCar(bus);
        journey.setAgencyBranch(agencyBranch);

        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setId(2L);
        AddStopDTO addStopDTO = new AddStopDTO();
        addStopDTO.setTransitAndStopId(3L);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
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
    void delete_journey_should_throw_journey_not_exist_api_exception() {
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.deleteNonBookedJourney(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * #169112562
     * Scenario: 2 Journey's arrival indicator is true
     */
    @Test
    void delete_journey_should_throw_journey_already_terminated_api_exception() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.deleteNonBookedJourney(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString()));
        assertThat(apiException.getMessage(), is("Journey already terminated"));
    }

    /**
     * #169112562
     * Scenario: 3 Journey's car is not in AuthUser Agency
     */
    @Test
    void delete_journey_should_throw_journey_not_in_auth_user_agency_branch_api_exception() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(1L);
        agencyBranch.setName("Main");

        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(new AgencyBranch());

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
        journey.setAgencyBranch(agencyBranch);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.deleteNonBookedJourney(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s car not in AuthUser\'s Agency"));
    }

    /**
     * #169114980
     * Scenario: 1. Journey not exist
     */
    @Test
    void set_journey_departure_indicator_should_throw_journey_not_exist_api_exception() {
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * #169114980
     * Scenario: 2. Journey already terminated
     */
    @Test
    void set_journey_departure_indicator_should_throw_journey_already_terminated_api_exception() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString()));
        assertThat(apiException.getMessage(), is("Journey already terminated"));
    }

    /**
     * #169114980
     * Scenario: 3 Journey's car is not in AuthUser Agency
     */
    @Test
    void set_journey_departure_indicator_should_throw_journey_not_in_auth_user_agency_api_exception() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");

        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(new AgencyBranch());

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
        journey.setAgencyBranch(agencyBranch);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourneyDepartureIndicator(4L, new JourneyDepartureIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s car not in AuthUser\'s Agency"));
    }

    /**
     * #169114979
     * Scenario: 1. Journey not exist
     */
    @Test
    void update_journey_arrival_indicator_should_throw_journey_not_exist_api_exception() {
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());
        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * #169114979
     * Scenario: 2. Journey not started
     */
    @Test
    void update_journey_departure_indicator_should_throw_journey_not_started_api_exception() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(false);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_NOT_STARTED.toString()));
        assertThat(apiException.getMessage(), is("Journey not started"));
    }

    /**
     * #169114979
     * Scenario: 3 Journey's car is not in AuthUser Agency
     */
    @Test
    void update_journey_arrival_indicator_should_throw_journey_not_in_auth_user_agency_api_exception() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");
        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(new AgencyBranch());

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
        journey.setAgencyBranch(agencyBranch);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourneyArrivalIndicator(4L, new JourneyArrivalIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s car not in AuthUser\'s Agency"));
    }

    @Test
    void updateJourneyArrivalIndicator_should_shouldUpDate_and_sendNotificationToPassengers() {

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("GMT+02:00"));
        GgCfsSurveyTemplateJson ggCfsSurveyTemplateJson = new GgCfsSurveyTemplateJson();
        ggCfsSurveyTemplateJson.setId("1");
        ggCfsSurveyTemplateJson.setSurveyTemplateJson("" +
                "{\n" +
                "  \"id\": \"d76cad8e-d126-47d5-8a69-47253c9bb0ba\",\n" +
                "  \"name\": \"GoWaka Journey, 10/12/2020 Musago expres\",\n" +
                "  \"description\": \"You are receiving this invite because you recently booked a trip using GoWaka. We actively use feedback to constantly improve our service and provide you with the best possible experience\",\n" +
                "  \"status\": true,\n" +
                "  \"createdAt\": \"2020-10-19 07:07:06\",\n" +
                "  \"surveyInputs\": [\n" +
                "    {\n" +
                "      \"id\": 13,\n" +
                "      \"title\": \"Please rate your experience using GoWaka\",\n" +
                "      \"surveyInputType\": \"FIVE_STAR\",\n" +
                "      \"required\": true,\n" +
                "      \"position\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 14,\n" +
                "      \"title\": \"Any concern or suggestion you will like us to know?\",\n" +
                "      \"surveyInputType\": \"TEXT\",\n" +
                "      \"required\": true,\n" +
                "      \"position\": 2\n" +
                "    }\n" +
                "  ]\n" +
                "}\n");

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(111L);


        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");
        User user = new User();
        user.setUserId("1");
        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(agencyBranch);

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("GG VIP1");
        bus.setOfficialAgency(officialAgency);
        officialAgency.setBuses(Collections.singletonList(bus));

        TransitAndStop departureLocation = new TransitAndStop();
        Location location = new Location();
        location.setAddress("Buea motto park");
        location.setCity("Buea");
        departureLocation.setLocation(location);

        TransitAndStop destinationLocation = new TransitAndStop();
        destinationLocation.setLocation(location);

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(true);
        journey.setCar(bus);
        journey.setDepartureLocation(departureLocation);
        journey.setDepartureLocation(departureLocation);
        journey.setAgencyBranch(agencyBranch);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setDestination(departureLocation);
        bookedJourney.setSmsNotification(Boolean.TRUE);
        Passenger passenger = new Passenger();
        passenger.setEmail("passenger@gmail.com");
        passenger.setPhoneNumber("237678787878");
        bookedJourney.setPassengers(Collections.singletonList(passenger));
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus("COMPLETED");
        bookedJourney.setPaymentTransaction(paymentTransaction);

        BookedJourney bookedJourney2 = new BookedJourney();
        bookedJourney2.setDestination(departureLocation);
        bookedJourney2.setSmsNotification(Boolean.TRUE);
        Passenger passenger2 = new Passenger();
        passenger2.setEmail("passenger2@gmail.com");
        passenger2.setPhoneNumber("237670909090");
        bookedJourney2.setPassengers(Collections.singletonList(passenger2));
        PaymentTransaction paymentTransaction2 = new PaymentTransaction();
        paymentTransaction2.setTransactionStatus("COMPLETED");
        RefundPaymentTransaction refundPaymentTransaction = new RefundPaymentTransaction();
        refundPaymentTransaction.setRefundStatus("REFUNDED");
        paymentTransaction2.setRefundPaymentTransaction(refundPaymentTransaction);
        bookedJourney2.setPaymentTransaction(paymentTransaction2);

        journey.setBookedJourneys(Arrays.asList(bookedJourney, bookedJourney2));

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockGgCfsSurveyTemplateJsonRepository.findById(anyString()))
                .thenReturn(Optional.of(ggCfsSurveyTemplateJson));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockEmailContentBuilder.buildJourneyStatusEmail(any()))
                .thenReturn("some update message");

        JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO = new JourneyArrivalIndicatorDTO();
        journeyArrivalIndicatorDTO.setArrivalIndicator(true);
        journey.setArrivalTime(localDateTime);
        journeyService.updateJourneyArrivalIndicator(1L, journeyArrivalIndicatorDTO);

        verify(mockJourneyRepository).save(any());
        verify(mockGgCfsSurveyTemplateJsonRepository).findById("1");
        verify(mockCfsClientService).createAndAddCustomerToSurvey(any(), any());
        verify(mockEmailContentBuilder).buildJourneyStatusEmail(any());

        ArgumentCaptor<SendEmailDTO> sendEmailDTOArgumentCaptor = ArgumentCaptor.forClass(SendEmailDTO.class);
        ArgumentCaptor<SendSmsDTO> sendSmsDTOArgumentCaptor = ArgumentCaptor.forClass(SendSmsDTO.class);
        ArgumentCaptor<Journey> journeyArgumentCaptor = ArgumentCaptor.forClass(Journey.class);
        verify(mockNotificationService).sendSMS(sendSmsDTOArgumentCaptor.capture());
        verify(mockNotificationService).sendEmail(sendEmailDTOArgumentCaptor.capture());
        verify(mockJourneyRepository).save(journeyArgumentCaptor.capture());

        assertThat(sendSmsDTOArgumentCaptor.getValue().getMessage(), is("Your trip to Buea on GoWaka just ended"));
        assertThat(sendSmsDTOArgumentCaptor.getValue().getPhoneNumber(), is("237678787878"));
        assertThat(sendSmsDTOArgumentCaptor.getValue().getSenderLabel(), is("GoWaka"));
        assertThat(journeyArgumentCaptor.getValue().getArrivalTime(),is(journey.getArrivalTime()));

        assertThat(sendEmailDTOArgumentCaptor.getValue().getMessage(), is("some update message"));
        assertThat(sendEmailDTOArgumentCaptor.getValue().getToAddresses().get(0).getEmail(), is("no-reply@mygowaka.com"));
        assertThat(sendEmailDTOArgumentCaptor.getValue().getToAddresses().get(0).getName(), is("no-reply@mygowaka.com"));
        assertThat(sendEmailDTOArgumentCaptor.getValue().getBccAddresses().get(0).getName(), is("passenger@gmail.com"));
        assertThat(sendEmailDTOArgumentCaptor.getValue().getBccAddresses().get(0).getEmail(), is("passenger@gmail.com"));
    }

    @Test
    void updateJourneyArrivalIndicator_throwsException_whenJourneyFinished() {

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(true);
        journey.setIsJourneyFinished(Boolean.TRUE);

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO = new JourneyArrivalIndicatorDTO();
        journeyArrivalIndicatorDTO.setArrivalIndicator(true);
        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourneyArrivalIndicator(1L, journeyArrivalIndicatorDTO));
        assertThat(apiException.getErrorCode(), is("JOURNEY_IS_FINISHED"));
        assertThat(apiException.getHttpStatus(), is(HttpStatus.UNPROCESSABLE_ENTITY));
        assertThat(apiException.getMessage(), is("The journey is closed."));

    }

    @Test
    void updateJourneyArrivalIndicator_should_shouldUpDate_butNot_sendNotificationToPassengers_whenArrivalIndIsFalse() {

        GgCfsSurveyTemplateJson ggCfsSurveyTemplateJson = new GgCfsSurveyTemplateJson();
        ggCfsSurveyTemplateJson.setId("1");
        ggCfsSurveyTemplateJson.setSurveyTemplateJson("" +
                "{\n" +
                "  \"id\": \"d76cad8e-d126-47d5-8a69-47253c9bb0ba\",\n" +
                "  \"name\": \"GoWaka Journey, 10/12/2020 Musago expres\",\n" +
                "  \"description\": \"You are receiving this invite because you recently booked a trip using GoWaka. We actively use feedback to constantly improve our service and provide you with the best possible experience\",\n" +
                "  \"status\": true,\n" +
                "  \"createdAt\": \"2020-10-19 07:07:06\",\n" +
                "  \"surveyInputs\": [\n" +
                "    {\n" +
                "      \"id\": 13,\n" +
                "      \"title\": \"Please rate your experience using GoWaka\",\n" +
                "      \"surveyInputType\": \"FIVE_STAR\",\n" +
                "      \"required\": true,\n" +
                "      \"position\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 14,\n" +
                "      \"title\": \"Any concern or suggestion you will like us to know?\",\n" +
                "      \"surveyInputType\": \"TEXT\",\n" +
                "      \"required\": true,\n" +
                "      \"position\": 2\n" +
                "    }\n" +
                "  ]\n" +
                "}\n");


        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(111L);


        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");
        User user = new User();
        user.setUserId("1");
        user.setOfficialAgency(officialAgency);
        user.setAgencyBranch(agencyBranch);

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("GG VIP1");
        bus.setOfficialAgency(officialAgency);
        officialAgency.setBuses(Collections.singletonList(bus));

        TransitAndStop departureLocation = new TransitAndStop();
        Location location = new Location();
        location.setAddress("Buea motto park");
        departureLocation.setLocation(location);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(true);
        journey.setCar(bus);
        journey.setDepartureLocation(departureLocation);
        journey.setAgencyBranch(agencyBranch);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        JourneyArrivalIndicatorDTO journeyArrivalIndicatorDTO = new JourneyArrivalIndicatorDTO();
        journeyArrivalIndicatorDTO.setArrivalIndicator(false);
        journeyService.updateJourneyArrivalIndicator(1L, journeyArrivalIndicatorDTO);
        verify(mockJourneyRepository).save(any());
        verifyZeroInteractions(mockGgCfsSurveyTemplateJsonRepository);
        verifyZeroInteractions(mockCfsClientService);
    }

    /**
     * **USERS** Should be able to SharedRide Journey
     * #169527991
     * scenario: 1 carId not in user's personal agency
     */
    @Test
    void personal_agency_add_journey_shared_rides_should_throw_not_found_api_exception() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addSharedJourney(new JourneyDTO(), 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s Car not in AuthUser\'s PersonalAgency"));
    }

    /**
     * **USERS** Should be able to SharedRide Journey
     * #169527991
     * scenario: 3 transitAndStopId don't exist
     */
    @Test
    void personal_agency_add_journey_shared_rides_should_throw_transit_and_stop_not_found_api_exception() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        SharedRide sharedRide = new SharedRide();
        sharedRide.setId(1L);
        when(mockPersonalAgency.getSharedRides()).thenReturn(Collections.singletonList(sharedRide));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addSharedJourney(new JourneyDTO(), 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Destination TransitAndStop not found"));
    }

    /**
     * **USERS** can update information about  Journey for PersonalAgency
     * #169528238
     * scenario: 1 Journey does not exist
     */
    @Test
    void personal_agency_update_journey_shared_rides_should_throw_journey_not_found_api_exception() {

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateSharedJourney(new JourneyDTO(), 1L, 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * **USERS** can update information about  Journey for PersonalAgency
     * #169528238
     * scenario: 2 Car not in AuthUser's PersonalAgency
     */
    @Test
    void personal_agency_update_journey_shared_rides_should_throw_car_not_found_api_exception() {
        user.setUserId("1");
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(new Journey()));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateSharedJourney(new JourneyDTO(), 1L, 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s Car not in AuthUser\'s PersonalAgency"));

    }

    /**
     * **USERS** can GET  a Journey for OfficialAgency
     * #169528470
     * Scenario: 1 Journeys Not exist
     */
    @Test
    void personal_agency_get_journey_should_throw_journey_not_exist_api_exception() {

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.getSharedJourneyById(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * **USERS** can GET  a Journey for OfficialAgency
     * #169528470
     * Scenario: 2 Journey's Car not in AuthUser's personalAgency
     */
    @Test
    void personal_agency_get_journey_should_throw_car_not_in_personal_agency_api_exception() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(new Journey()));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.getSharedJourneyById(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s Car not in AuthUser\'s PersonalAgency"));
    }

    /**
     * **USERS**  can view all Journey for PersonalAgency ordered by date and arrivalIndicator
     * #169528531
     * Scenario: 1. No Journey found for user's Agency
     */
    @Test
    void Given_No_journey_exist_for_that_user_personal_agency() {

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
        when(mockJourneyRepository.findAllByOrderByCreatedAtDescArrivalIndicatorAsc()).thenReturn(Collections.singletonList(journey));
        List<JourneyResponseDTO> journeyList = journeyService.getAllPersonalAgencyJourneys();
        assertTrue(journeyList.isEmpty());
    }

    /**
     * USERS can change departureIndicator state
     * #169528573
     * Scenario:  1. Journeys NOT exist
     */
    @Test
    void Journeys_NOT_exist_Given_JourneyId_passed_as_parameter_NOT_exit() {

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateSharedJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    @Test
    void updateJourneyDepartureIndicator_throwsException_whenJourneyFinished() {

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setDepartureIndicator(true);
        journey.setIsJourneyFinished(Boolean.TRUE);

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        JourneyDepartureIndicatorDTO journeyDepartureIndicatorDTO = new JourneyDepartureIndicatorDTO();
        journeyDepartureIndicatorDTO.setDepartureIndicator(true);
        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateJourneyDepartureIndicator(1L, journeyDepartureIndicatorDTO));
        assertThat(apiException.getErrorCode(), is("JOURNEY_IS_FINISHED"));
        assertThat(apiException.getHttpStatus(), is(HttpStatus.UNPROCESSABLE_ENTITY));
        assertThat(apiException.getMessage(), is("The journey is closed."));

    }

    /**
     * USERS can change departureIndicator state
     * #169528573
     * Scenario:  3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    void Journey_car_is_NOT_in_AuthUser_Agency_Given_journeyId_passed_and_arrivalIndicator_false() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateSharedJourneyDepartureIndicator(1L, new JourneyDepartureIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s Car not in AuthUser\'s PersonalAgency"));
    }

    /**
     * USERS can change departureIndicator state
     * #169528573
     * Scenario:  2. Journey's arrivalIndicator is true;
     */
    @Test
    void given_journeyId_passed_as_parameter_exist_and_journey_arrivalIndicator_true_throw_journey_terminated_exception() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateSharedJourneyDepartureIndicator(journey.getId(), new JourneyDepartureIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString()));
        assertThat(apiException.getMessage(), is("Journey already terminated"));
    }

    /**
     * USERS can change departureIndicator state
     * #169528573
     * Scenario:  4. change  Journey departureIndicator state
     */
    @Test
    void given_journeyId_passed_as_parameter_exist_and_journey_arrivalIndicator_false_and_Journey_car_is_IN_AuthUser_Agency_then_change_departure_state() {
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
    void Given_JourneyId_passed_as_parameter_Journeys_NOT_exis() {

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateSharedJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * **USERS** can change arrivalIndicator stat
     * #169528624
     * Scenario:  2. Journey's departure is false;
     */
    @Test
    void journeyId_passed_as_parameter_exist_and_Journey_departure_Indicator_false() {
        Journey journey = new Journey();
        journey.setDepartureIndicator(false);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateSharedJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_NOT_STARTED.toString()));
        assertThat(apiException.getMessage(), is("Journey not started"));
    }

    /**
     * **USERS** can change arrivalIndicator stat
     * #169528624
     * Scenario:  3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    void journeyId_as_parameter_exist_and_journey_departureIndicator_true_journey_car_not_in_authUser_agency() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Journey journey = new Journey();
        journey.setDepartureIndicator(true);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.updateSharedJourneyArrivalIndicator(1L, new JourneyArrivalIndicatorDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s Car not in AuthUser\'s PersonalAgency"));
    }

    /**
     * **USERS** can change arrivalIndicator stat
     * #169528624
     * Scenario:  4. change  Journey arrivalIndicator state
     */
    @Test
    void journeyId_as_parameter_exist_and_journey_departureIndicator_true_journey_car_is_in_authUser_agency() {
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
    void given_JourneyId_passed_as_parameter_not_exit() {
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.deleteNonBookedSharedJourney(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * **USERS** can delete  Journey for PersonalAgency  if NO booking and arrivalIndicator = false
     * #169528640
     * Scenario:  2. Journey's arrivalIndicator is truet
     */
    @Test
    void given_journeyId_passed_as_parameter_exit_and_journey_arrival_indicator_true_journey_already_terminated() {
        Journey journey = new Journey();
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.deleteNonBookedSharedJourney(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString()));
        assertThat(apiException.getMessage(), is("Journey already terminated"));
    }

    /**
     * **USERS** can delete  Journey for PersonalAgency  if NO booking and arrivalIndicator = false
     * #169528640
     * Scenario:  3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    void given_journeyId_passed_as_parameter_exist_and_journey_arrivalIndicator_false_but_journey_car_is_not_in_authUser_agency() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Journey journey = new Journey();
        journey.setArrivalIndicator(false);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.deleteNonBookedSharedJourney(1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s Car not in AuthUser\'s PersonalAgency"));
    }

    /**
     * **USERS** can delete  Journey for PersonalAgency  if NO booking and arrivalIndicator = false
     * #169528640
     * Scenario:  4. Delete Journey Success
     */
    @Test
    void delete_shared_journey_should_call_journey_repository_delete() {
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
    void throw_journey_already_exist_given_arrivalIndicator_is_true_with_id() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addStopToPersonalAgency(1L, new AddStopDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString()));
        assertThat(apiException.getMessage(), is("Journey already terminated"));
    }

    /**
     * **USERS** can add Journey STOPS  Journey for PersonalAgency  if  arrivalIndicator = false
     * #169528838
     * Scenario:  2. Journeys NOT exist
     */
    @Test
    void throw_error_resource_not_found_given_journey_does_not_exist() {
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addStopToPersonalAgency(1L, new AddStopDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));

    }

    /**
     * **USERS** can add Journey STOPS  Journey for PersonalAgency  if  arrivalIndicator = false
     * #169528838
     * Scenario:  3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    void throw_resource_not_found_given_arrivalIndicator_is_false_with_id_car_is_not_in_autUser_agency() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(false);
        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(user.getPersonalAgency()).thenReturn(mockPersonalAgency);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addStopToPersonalAgency(1L, new AddStopDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s Car not in AuthUser\'s PersonalAgency"));
    }

    /**
     * **USERS** can add Journey STOPS  Journey for PersonalAgency  if  arrivalIndicator = false
     * #169528838
     * Scenario:  4. TransitAndStopId don't exit
     */

    @Test
    void throw_transit_and_stop_not_found_given_transit_and_stop_false_car_not_in_authUser_agency() {
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

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.addStopToPersonalAgency(1L, new AddStopDTO()));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("TransitAndStop not found"));
    }

    /**
     * **add Search Journey api endpoint
     * #169528838
     * Scenario: 1. Wrong input data passed
     */
    @Test
    void throw_bad_request_given_wrong_date_time_input_data_passed_when_during_search() {

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.searchJourney(1L, 1L, "9999999"));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.INVALID_FORMAT.toString()));
        assertThat(apiException.getMessage(), is("Invalid date time format. Try yyyy-MM-dd HH:mm"));

    }

    /**
     * **add Search Journey api endpoint
     * #169528838
     * Scenario: 1. Wrong input data passed
     */
    @Test
    void throw_bad_request_given_wrong_departure_id_input_data_passed_when_during_search() {
        when(mockTransitAndStopRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.searchJourney(1L, 1L, "2020-01-20 12:30"));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Departure TransitAndStop not found"));

    }

    /**
     * **add Search Journey api endpoint
     * #169528838
     * Scenario: 1. Wrong input data passed
     */
    @Test
    void throw_bad_request_given_wrong_destination_id_input_data_passed_when_during_search() {
        when(mockTransitAndStopRepository.findById(1L)).thenReturn(Optional.of(new TransitAndStop()));
        when(mockTransitAndStopRepository.findById(2L)).thenReturn(Optional.empty());

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.searchJourney(1L, 2L, "2020-01-20 12:30"));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Destination TransitAndStop not found"));


    }

    /**
     * AGENCY_MANAGER or AGENCY_OPERATOR** can remove STOPS
     * or Updating Journey  for Journey in their OfficialAgency  if  arrivalIndicator = false and NO booking
     * #169112817
     * Scenario: 1. Journeys NOT exist
     */
    @Test
    void given_journey_id_then_throw_not_found_exception_if_journey_not_exist() {

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.removeNonBookedStop(1L, 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey not found"));
    }

    /**
     * AGENCY_MANAGER or AGENCY_OPERATOR** can remove STOPS
     * or Updating Journey  for Journey in their OfficialAgency  if  arrivalIndicator = false and NO booking
     * #169112817
     * Scenario: 2. Journey's arrivalIndicator is true
     */
    @Test
    void given_journey_arrival_indicator_true_then_throw_journey_already_terminated_exception() {
        Journey journey = new Journey();
        journey.setId(1L);
        journey.setArrivalIndicator(true);
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.removeNonBookedStop(1L, 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.JOURNEY_ALREADY_TERMINATED.toString()));
        assertThat(apiException.getMessage(), is("Journey already terminated"));

    }

    /**
     * AGENCY_MANAGER or AGENCY_OPERATOR** can remove STOPS
     * or Updating Journey  for Journey in their OfficialAgency  if  arrivalIndicator = false and NO booking
     * #169112817
     * Scenario: 3. Journey's car is NOT in AuthUser Agency
     */
    @Test
    void given_journey_branch_not_in_user_agency_then_throw_not_found_exception() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(1L);
        agencyBranch.setName("Main");

        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(new AgencyBranch());
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
        journey.setAgencyBranch(agencyBranch);

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.removeNonBookedStop(1L, 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.RESOURCE_NOT_FOUND.toString()));
        assertThat(apiException.getMessage(), is("Journey\'s car not in AuthUser\'s Agency"));

    }

    /**
     * AGENCY_MANAGER or AGENCY_OPERATOR** can remove STOPS
     * or Updating Journey  for Journey in their OfficialAgency  if  arrivalIndicator = false and NO booking
     * #169112817
     * Scenario: 4. Some users already booked to that stop location
     */
    @Test
    void given_journey_has_booking_for_transit_and_stop_then_throw_transit_and_stop_already_booked_exception() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(1L);
        agencyBranch.setName("Main");

        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(agencyBranch);

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
        journey.setAgencyBranch(agencyBranch);

        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setId(1L);

        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setJourney(journey);
        bookedJourney.setDestination(transitAndStop);
        transitAndStop.setBookedJourneys(Collections.singletonList(bookedJourney));

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockTransitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));

        ApiException apiException = assertThrows(ApiException.class, () -> journeyService.removeNonBookedStop(1L, 1L));
        assertThat(apiException.getErrorCode(), is(ErrorCodes.TRANSIT_AND_STOP_ALREADY_BOOKED.toString()));
        assertThat(apiException.getMessage(), is(ErrorCodes.TRANSIT_AND_STOP_ALREADY_BOOKED.getMessage()));

    }

    @Test
    void searchJourney_noParam() {

        UserDTO userDto = new UserDTO();
        userDto.setId("12");
        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDto);
        BookedJourney bookedJourney = new BookedJourney();
        TransitAndStop destination = new TransitAndStop();
        TransitAndStop departure = new TransitAndStop();
        Journey journey = new Journey();

        journey.setDepartureLocation(departure);
        journey.setDestination(destination);
        journey.setDepartureTime(LocalDateTime.now());
        destination.setId(2L);
        departure.setId(1L);
        bookedJourney.setDestination(destination);
        bookedJourney.setJourney(journey);

        when(mockBookedJourneyRepository.findTopByUser_UserIdOrderByIdDesc(anyString()))
                .thenReturn(Optional.of(bookedJourney));
        when(mockJourneyRepository.findAllByDepartureIndicatorFalseOrderByDepartureTimeAsc())
                .thenReturn(Collections.singletonList(journey));

        when(mockTransitAndStopRepository.findById(1L))
                .thenReturn(Optional.of(departure));
        when(mockTransitAndStopRepository.findById(2L))
                .thenReturn(Optional.of(destination));

        List<JourneyResponseDTO> journeyResponseDTOS = journeyService.searchJourney();

        verify(mockUserService).getCurrentAuthUser();
        verify(mockTransitAndStopRepository, times(2)).findById(1L);
        verify(mockTransitAndStopRepository, times(2)).findById(2L);
        verify(mockBookedJourneyRepository).findTopByUser_UserIdOrderByIdDesc("12");

        assertThat(journeyResponseDTOS.size(), is(1));
    }

    @Test
    void searchAllAvailableJourney_return_all_journey_withDepartureIndicatorFalse() {

        BookedJourney bookedJourney = new BookedJourney();
        TransitAndStop destination = new TransitAndStop();
        TransitAndStop departure = new TransitAndStop();
        Journey journey = new Journey();

        journey.setDepartureLocation(departure);
        journey.setDestination(destination);
        journey.setDepartureTime(LocalDateTime.now());
        destination.setId(2L);
        departure.setId(1L);
        bookedJourney.setDestination(destination);
        bookedJourney.setJourney(journey);

        when(mockJourneyRepository.findAllByDepartureIndicatorFalseOrderByDepartureTimeAsc())
                .thenReturn(Collections.singletonList(journey));

        List<JourneyResponseDTO> journeyResponseDTOS = journeyService.searchAllAvailableJourney();
        verify(mockJourneyRepository).findAllByDepartureIndicatorFalseOrderByDepartureTimeAsc();
        assertThat(journeyResponseDTOS.size(), is(1));
    }

    @Test
    void whenJourneyCarSeatStructureChange_thenCallSendSMSNotification_withAppropriateMessage() {

        AgencyBranch agencyBranch = new AgencyBranch();
        agencyBranch.setId(15L);
        agencyBranch.setName("Main Branch");
        User user = new User();
        user.setUserId("1");
        user.setAgencyBranch(agencyBranch);
        OfficialAgency officialAgency = new OfficialAgency();
        user.setOfficialAgency(officialAgency);

        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");

        TransitAndStop departure = new TransitAndStop();
        departure.setId(13L);
        Location location = new Location();
        location.setCity("Buea");
        departure.setLocation(location);

        TransitAndStop destination = new TransitAndStop();
        destination.setId(1L);
        destination.setLocation(new Location());

        Bus bus = new Bus();
        bus.setId(1L);
        bus.setName("Muea boy");
        bus.setNumberOfSeats(30);
        SeatStructure seatStructure = new SeatStructure();
        seatStructure.setSeatStructureCode("CODE1");
        seatStructure.setNumberOfSeats(30);
        seatStructure.setImage("image");
        bus.setSeatStructure(seatStructure);
        Bus bus1 = new Bus();
        bus1.setId(2L);
        bus1.setName("Happi");
        bus1.setNumberOfSeats(70);
        SeatStructure seatStructure1 = new SeatStructure();
        seatStructure1.setSeatStructureCode("CODE1");
        seatStructure1.setNumberOfSeats(70);
        seatStructure1.setImage("image");
        bus1.setSeatStructure(seatStructure);
        Journey journey = new Journey();
        journey.setArrivalIndicator(false);
        journey.setCar(bus1);
        journey.setId(1L);
        journey.setAgencyBranch(agencyBranch);
        journey.setDepartureLocation(departure);
        BookedJourney bookedJourney = new BookedJourney();
        bookedJourney.setSmsNotification(true);
        bookedJourney.setDestination(destination);
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setTransactionStatus("COMPLETED");
        Passenger passenger = new Passenger();
        passenger.setPhoneNumber("237777777777");
        passenger.setEmail("hello@helo.com");
        bookedJourney.setPassengers(Collections.singletonList(passenger));
        bookedJourney.setPaymentTransaction(paymentTransaction);
        journey.setBookedJourneys(Collections.singletonList(bookedJourney));

        officialAgency.setBuses(Collections.singletonList(bus));

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));
        when(mockJourneyRepository.findById(anyLong())).thenReturn(Optional.of(journey));
        when(mockTransitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(departure));
        when(mockJourneyRepository.save(any())).thenReturn(journey);
        when(mockEmailContentBuilder.buildJourneyStatusEmail(any()))
                .thenReturn("email message");
        JourneyDTO journeyDTO = new JourneyDTO();
        AddStopDTO addStopDTO = new AddStopDTO();
        addStopDTO.setTransitAndStopId(1L);
        journeyDTO.setDestination(addStopDTO);
        journeyDTO.setDepartureLocation(1L);
        journeyDTO.setTransitAndStops(Collections.singletonList(addStopDTO));
        journeyService.updateJourney(journeyDTO, journey.getId(), bus.getId());
        verify(mockEmailContentBuilder).buildJourneyStatusEmail(any());
        verify(mockNotificationService).sendEmail(any());
        verify(mockNotificationService).sendSMS(any());
        verify(mockGwCacheLoaderService).addUpdateJourney(any());
    }

}
