package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.model.*;
import net.gogroups.gowaka.domain.repository.TransitAndStopRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.LocationResponseDTO;
import net.gogroups.gowaka.dto.LocationDTO;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.service.TransitAndStopService;
import net.gogroups.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */

@RunWith(MockitoJUnitRunner.class)
public class TransitAndStopImplTest {



    private TransitAndStopService transitAndStopService;

    @Mock
    private UserService mockUserService;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private User user;

    @Mock
    private TransitAndStopRepository transitAndStopRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup(){
        transitAndStopService = new TransitAndStopServiceServiceImpl(transitAndStopRepository);
    }

    @Test
    public void should_save_new_transit_and_stop_entity(){
        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        when(transitAndStopRepository.findDistinctFirstByLocation(location))
                .thenReturn(Optional.empty());
        when(transitAndStopRepository.save(any())).thenReturn(new TransitAndStop());
        transitAndStopService.addLocation(locationDTO);
        verify(transitAndStopRepository).save(transitAndStop);
    }

    @Test
    public void should_throw_transit_and_stop_already_in_use_api_exception(){
        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        when(transitAndStopRepository.findDistinctFirstByLocation(location))
                .thenReturn(Optional.of(transitAndStop));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop already Exists");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.TRANSIT_AND_STOP_ALREADY_IN_USE.toString())));
        transitAndStopService.addLocation(locationDTO);
    }
    @Test
    public void should_update_transit_and_stop(){
        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStop.setId(1L);
        when(transitAndStopRepository.findDistinctFirstByLocation(location))
                .thenReturn(Optional.empty());
        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        when(transitAndStopRepository.save(any())).thenReturn(new TransitAndStop());
        transitAndStopService.updateLocation(1L, locationDTO);
        verify(transitAndStopRepository).save(transitAndStop);
    }

    @Test
    public void update_location_should_throw_transit_and_stop_not_found_api_exception(){
        LocationDTO locationDTO = new LocationDTO();
        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        transitAndStopService.updateLocation(1L, locationDTO);
    }
    @Test
    public void update_location_should_throw_transit_and_stop_already_in_use_exception(){
       LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStop.setId(1L);
        when(transitAndStopRepository.findDistinctFirstByLocation(location))
                .thenReturn(Optional.of(transitAndStop));
        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop already Exists");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.TRANSIT_AND_STOP_ALREADY_IN_USE.toString())));
        transitAndStopService.updateLocation(1L, locationDTO);
    }

    @Test
    public void delete_transit_should_throw_journey_exist_api_exception(){
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setJourneyStops(Collections.singletonList(new JourneyStop()));
        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("Cannot delete record for any existing journey");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.VALIDATION_ERROR.toString())));
        transitAndStopService.deleteLocation(1L);
    }

    @Test
    public void delete_transit_should_throw_transit_not_found_exception(){
       when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        expectedException.expectMessage("TransitAndStop not found");
        transitAndStopService.deleteLocation(1L);
    }
    @Test
    public void delete_transit_should_call_delete_by_id(){
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setId(1L);

        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        transitAndStopService.deleteLocation(transitAndStop.getId());
        verify(transitAndStopRepository).deleteById(transitAndStop.getId());
    }

    @Test
    public void get_all_transit_and_stop_locations_should_return_location_list(){
        TransitAndStop transitAndStop = new TransitAndStop();
        Location location = new Location();
        location.setState("SW");
        transitAndStop.setLocation(location);
        transitAndStop.setId(4L);
        when(transitAndStopRepository.findAll()).thenReturn(Collections.singletonList(transitAndStop));
        List<LocationResponseDTO> locationResponseDTOList = transitAndStopService.getAllLocations();
        assertThat(locationResponseDTOList.get(0).getId(), is(transitAndStop.getId()));
        assertThat(locationResponseDTOList.get(0).getState(), is(location.getState()));
    }

    @Test
    public void search_transit_and_stop_by_city_should_return_location_list(){
        TransitAndStop transitAndStop = new TransitAndStop();
        Location location = new Location();
        location.setState("SW");
        location.setCity("Buea");
        transitAndStop.setLocation(location);
        transitAndStop.setId(4L);
        when(transitAndStopRepository.findByLocationCityIgnoreCase(anyString()))
                .thenReturn(Collections.singletonList(transitAndStop));
        List<LocationResponseDTO> locationResponseDTOList = transitAndStopService.searchByCity("Buea");
        assertThat(locationResponseDTOList.get(0).getId(), is(transitAndStop.getId()));
        assertThat(locationResponseDTOList.get(0).getState(), is(location.getState()));
    }

    /**
     * Journey departure Location to use Transit and Stop Location Id
     * #171379777
     * restrict stop updates to non booked journeys
     */
    @Test
    public void update_stop_should_throw_location_has_booked_journey_api_exception() {
        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStop.setId(1L);
        transitAndStop.setBookedJourneys(Collections.singletonList(new BookedJourney()));
        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage(ErrorCodes.LOCATION_HAS_BOOKED_JOURNEY.getMessage());
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.LOCATION_HAS_BOOKED_JOURNEY.toString())));
        transitAndStopService.updateLocation(1L, locationDTO);
    }
}
