package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.config.ClientUserCredConfig;
import net.gowaka.gowaka.domain.model.OfficialAgency;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.OfficialAgencyRepository;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.CreateOfficialAgencyDTO;
import net.gowaka.gowaka.dto.OfficialAgencyAdminUserDTO;
import net.gowaka.gowaka.dto.OfficialAgencyDTO;
import net.gowaka.gowaka.exception.ApiException;
import net.gowaka.gowaka.exception.ErrorCodes;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityAccessToken;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityClientUser;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUser;
import net.gowaka.gowaka.service.ApiSecurityService;
import net.gowaka.gowaka.service.OfficialAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static net.gowaka.gowaka.constant.GlobalConstants.AGENCY_ADMIN;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:23 PM <br/>
 */
@Service
public class OfficialAgencyServiceImpl implements OfficialAgencyService {

    private OfficialAgencyRepository officialAgencyRepository;
    private UserRepository userRepository;
    private ApiSecurityService apiSecurityService;
    private ClientUserCredConfig clientUserCredConfig;

    @Autowired
    public OfficialAgencyServiceImpl(OfficialAgencyRepository officialAgencyRepository, UserRepository userRepository, ApiSecurityService apiSecurityService, ClientUserCredConfig clientUserCredConfig) {
        this.officialAgencyRepository = officialAgencyRepository;
        this.userRepository = userRepository;
        this.apiSecurityService = apiSecurityService;
        this.clientUserCredConfig = clientUserCredConfig;
    }

    @Override
    public OfficialAgencyDTO createOfficialAgency(CreateOfficialAgencyDTO createOfficialAgencyDTO) {

        //make a call to get/verify the AgencyAdmin user
        // make a call to add AGENCY_ADMIN to user's role
        // add user to agency and save agency
        // create DTO and return

        ApiSecurityClientUser apiSecurityClientUser = new ApiSecurityClientUser();
        apiSecurityClientUser.setClientId(clientUserCredConfig.getClientId());
        apiSecurityClientUser.setClientSecret(clientUserCredConfig.getClientSecret());
        ApiSecurityAccessToken clientToken = apiSecurityService.getClientToken(apiSecurityClientUser);

        String username = createOfficialAgencyDTO.getAgencyAdminEmail();
        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUsername(username, clientToken.getToken());


        String userRole = apiSecurityUser.getRoles()+";"+AGENCY_ADMIN;
        apiSecurityService.updateUserInfo(apiSecurityUser.getId(), "ROLES", userRole, clientToken.getToken());

        Optional<User> userOptional = userRepository.findById(apiSecurityUser.getId());
        if (!userOptional.isPresent()) {
            throw new ApiException("Agency admin user not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userOptional.get();

        OfficialAgency officialAgency = new OfficialAgency();
        officialAgency.setAgencyName(createOfficialAgencyDTO.getAgencyName());
        officialAgency.setAgencyRegistrationNumber(createOfficialAgencyDTO.getAgencyRegistrationNumber());
        List<User> agencyUsers = officialAgency.getUsers();
        agencyUsers.add(user);
        officialAgency.setIsDisabled(false);

        OfficialAgency saveOfficialAgency = officialAgencyRepository.save(officialAgency);

        user.setOfficialAgency(officialAgency);
        userRepository.save(user);


        OfficialAgencyDTO officialAgencyDTO = new OfficialAgencyDTO();
        OfficialAgencyAdminUserDTO agencyAdminDTO = new OfficialAgencyAdminUserDTO();
        agencyAdminDTO.setId(apiSecurityUser.getId());
        agencyAdminDTO.setFullName(apiSecurityUser.getFullName());
        officialAgencyDTO.setId(saveOfficialAgency.getId());
        officialAgencyDTO.setAgencyName(saveOfficialAgency.getAgencyName());
        officialAgencyDTO.setAgencyRegistrationNumber(saveOfficialAgency.getAgencyRegistrationNumber());
        officialAgencyDTO.setAgencyAdmin(agencyAdminDTO);

        return officialAgencyDTO;

    }

}