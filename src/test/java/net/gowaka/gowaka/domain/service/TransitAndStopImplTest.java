package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.model.Location;
import net.gowaka.gowaka.domain.model.TransitAndStop;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.TransitAndStopRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.LocationDTO;
import net.gowaka.gowaka.dto.UserDTO;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.service.TransitAndStopService;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.Mockito.*;

/**
 * @author Nnouka Stephen
 * @date 07 Oct 2019
 */
@SpringBootTest
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
        transitAndStopService = new TransitAndStopServiceServiceImpl(transitAndStopRepository, mockUserRepository, mockUserService);
    }

    @Test
    public void should_save_new_transit_and_stop_entity(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        user.setUserId("1");

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        when(transitAndStopRepository.findDistinctByLocation(location))
                .thenReturn(Optional.empty());
        when(transitAndStopRepository.save(any())).thenReturn(new TransitAndStop());
        transitAndStopService.addLocation(locationDTO);
        verify(transitAndStopRepository).save(transitAndStop);
    }

    @Test
    public void should_throw_transit_and_stop_already_in_use_api_exception(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        user.setUserId("1");

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        when(transitAndStopRepository.findDistinctByLocation(location))
                .thenReturn(Optional.of(transitAndStop));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop already Exists");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.TRANSIT_AND_STOP_ALREADY_IN_USE.toString())));
        transitAndStopService.addLocation(locationDTO);
    }
    @Test
    public void should_update_transit_and_stop(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        user.setUserId("1");

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStop.setId(1L);
        when(transitAndStopRepository.findDistinctByLocation(location))
                .thenReturn(Optional.empty());
        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        when(transitAndStopRepository.save(any())).thenReturn(new TransitAndStop());
        transitAndStopService.updateLocation(1L, locationDTO);
        verify(transitAndStopRepository).save(transitAndStop);
    }
    @Test
    public void update_location_should_throw_transit_and_stop_not_found_api_exception(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        user.setUserId("1");

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.empty());
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop not found");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.RESOURCE_NOT_FOUND.toString())));
        transitAndStopService.updateLocation(1L, locationDTO);
    }
    @Test
    public void update_location_should_throw_transit_and_stop_already_in_use_exception(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId("1");
        user.setUserId("1");

        when(mockUserService.getCurrentAuthUser()).thenReturn(userDTO);
        when(mockUserRepository.findById(userDTO.getId())).thenReturn(Optional.of(user));

        LocationDTO locationDTO = new LocationDTO();
        Location location = new Location();
        TransitAndStop transitAndStop = new TransitAndStop();
        transitAndStop.setLocation(location);
        transitAndStop.setId(1L);
        when(transitAndStopRepository.findDistinctByLocation(location))
                .thenReturn(Optional.of(transitAndStop));
        when(transitAndStopRepository.findById(anyLong())).thenReturn(Optional.of(transitAndStop));
        expectedException.expect(ApiException.class);
        expectedException.expectMessage("TransitAndStop already Exists");
        expectedException.expect(hasProperty("errorCode", is(ErrorCodes.TRANSIT_AND_STOP_ALREADY_IN_USE.toString())));
        transitAndStopService.updateLocation(1L, locationDTO);
    }
}
