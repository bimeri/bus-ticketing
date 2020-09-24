package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.model.PersonalAgency;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.domain.repository.PersonalAgencyRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.CreatePersonalAgencyDTO;
import net.gogroups.gowaka.dto.UserDTO;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.PersonalAgencyService;
import net.gogroups.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 2:29 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class PersonalAgencyServiceImplTest {


    @Mock
    private UserService mockUserService;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private PersonalAgencyRepository mockPersonalAgencyRepository;

    private PersonalAgencyService personalAgencyService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setUp() throws Exception {
        personalAgencyService = new PersonalAgencyServiceImpl(mockUserService, mockUserRepository, mockPersonalAgencyRepository);
    }

    @Test
    public void createPersonalAgency_Calls_PersonalAgencyRepository() {

        ArgumentCaptor<PersonalAgency> personalAgencyArgumentCaptor = ArgumentCaptor.forClass(PersonalAgency.class);

        UserDTO userDTO = new UserDTO();
        userDTO.setId("12L");

        when(mockUserService.getCurrentAuthUser())
                .thenReturn(userDTO);
        User user = new User();
        user.setUserId("12L");
        when(mockUserRepository.findById(anyString()))
                .thenReturn(Optional.of(user));
        when(mockPersonalAgencyRepository.save(any()))
                .thenReturn(new PersonalAgency());

        CreatePersonalAgencyDTO createPersonalAgencyDTO = new CreatePersonalAgencyDTO();
        createPersonalAgencyDTO.setName("Papa Boss ShareRide");
        personalAgencyService.createPersonalAgency(createPersonalAgencyDTO);

        verify(mockPersonalAgencyRepository).save(personalAgencyArgumentCaptor.capture());

        PersonalAgency personalAgencyValue = personalAgencyArgumentCaptor.getValue();
        assertThat(personalAgencyValue.getName()).isEqualTo("Papa Boss ShareRide");
        assertThat(personalAgencyValue.getUser()).isEqualTo(user);

    }


    @Test
    public void createPersonalAgency_throw_exception_when() {

        when(mockUserService.getCurrentAuthUser())
                .thenReturn(new UserDTO());

        expectedException.expect(ResourceNotFoundException.class);
        expectedException.expectMessage("User not found.");
        personalAgencyService.createPersonalAgency(new CreatePersonalAgencyDTO());

    }
}
