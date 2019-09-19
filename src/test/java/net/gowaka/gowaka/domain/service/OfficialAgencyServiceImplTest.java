package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.config.ClientUserCredConfig;
import net.gowaka.gowaka.domain.model.OfficialAgency;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.OfficialAgencyRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.CreateOfficialAgencyDTO;
import net.gowaka.gowaka.dto.OfficialAgencyDTO;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityAccessToken;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUser;
import net.gowaka.gowaka.service.ApiSecurityService;
import net.gowaka.gowaka.service.OfficialAgencyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 9:29 PM <br/>
 */
@RunWith(MockitoJUnitRunner.class)
public class OfficialAgencyServiceImplTest {

    @Mock
    private OfficialAgencyRepository mockOfficialAgencyRepository;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private ApiSecurityService mockApiSecurityService;

    private ClientUserCredConfig clientUserCredConfig;

    private OfficialAgencyService officialAgencyService;


    @Before
    public void setUp() throws Exception {
        this.clientUserCredConfig = new ClientUserCredConfig();
        this.clientUserCredConfig.setClientId("client-id");
        this.clientUserCredConfig.setClientId("client-secret");
        this.clientUserCredConfig.setAppName("GoWaka");

        officialAgencyService = new OfficialAgencyServiceImpl(mockOfficialAgencyRepository, mockUserRepository, mockApiSecurityService, clientUserCredConfig);
    }

    @Test
    public void createOfficialAgency_call_OfficialAgencyRepository() {


        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<OfficialAgency> officialAgencyArgumentCaptor = ArgumentCaptor.forClass(OfficialAgency.class);
        ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fieldArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> roleArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tokenArgumentCaptor = ArgumentCaptor.forClass(String.class);

        CreateOfficialAgencyDTO createOfficialAgencyDTO = new CreateOfficialAgencyDTO();
        createOfficialAgencyDTO.setAgencyAdminEmail("example@example.com");
        createOfficialAgencyDTO.setAgencyName("Amo Mezam");
        createOfficialAgencyDTO.setAgencyRegistrationNumber("ABC20111234");

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setId("12");
        apiSecurityUser.setRoles("users");
        apiSecurityUser.setEmail("example@example.com");
        apiSecurityUser.setFullName("Jesus Christ");
        apiSecurityUser.setUsername("example@example.com");

        when(mockApiSecurityService.getUserByUsername(any()))
                .thenReturn(apiSecurityUser);

        ApiSecurityAccessToken accessToken = new ApiSecurityAccessToken();
        accessToken.setToken("jwt-token");
        when(mockApiSecurityService.getClientToken(any()))
                .thenReturn(accessToken);

        User user = new User();
        user.setUserId("12");
        when(mockUserRepository.findById(any()))
                .thenReturn(Optional.of(user));
        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setId(1L);
        officialAgency.setAgencyRegistrationNumber("ABC20111234");
        officialAgency.setAgencyName("Amo Mezam");
        when(mockOfficialAgencyRepository.save(any()))
                .thenReturn(officialAgency);

        OfficialAgencyDTO officialAgencyDTO = officialAgencyService.createOfficialAgency(createOfficialAgencyDTO);

        verify(mockApiSecurityService).updateUserInfo(idArgumentCaptor.capture(), fieldArgumentCaptor.capture(),
                roleArgumentCaptor.capture(), tokenArgumentCaptor.capture());
        assertThat(idArgumentCaptor.getValue()).isEqualTo("12");
        assertThat(fieldArgumentCaptor.getValue()).isEqualTo("ROLES");
        assertThat(roleArgumentCaptor.getValue()).isEqualTo("users;agency_admin");
        assertThat(tokenArgumentCaptor.getValue()).isEqualTo("jwt-token");

        verify(mockUserRepository).findById("12");

        verify(mockUserRepository).save(userArgumentCaptor.capture());
        assertThat(userArgumentCaptor.getValue().getUserId()).isEqualTo("12");

        verify(mockOfficialAgencyRepository).save(officialAgencyArgumentCaptor.capture());
        assertThat(officialAgencyArgumentCaptor.getValue().getAgencyName()).isEqualTo("Amo Mezam");
        assertThat(officialAgencyArgumentCaptor.getValue().getAgencyRegistrationNumber()).isEqualTo("ABC20111234");
        assertThat(officialAgencyArgumentCaptor.getValue().getUsers().size()).isEqualTo(1);
        assertThat(officialAgencyArgumentCaptor.getValue().getIsDisabled()).isEqualTo(false);


        assertThat(officialAgencyDTO.getAgencyAdmin()).isNotNull();
        assertThat(officialAgencyDTO.getAgencyName()).isEqualTo("Amo Mezam");
        assertThat(officialAgencyDTO.getAgencyRegistrationNumber()).isEqualTo("ABC20111234");


    }
}