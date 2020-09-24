package net.gogroups.gowaka.domain.service;

import io.jsonwebtoken.lang.Collections;
import net.gogroups.gowaka.domain.config.ClientUserCredConfig;
import net.gogroups.gowaka.domain.model.OfficialAgency;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.domain.repository.OfficialAgencyRepository;
import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.*;
import net.gogroups.security.model.ApiSecurityAccessToken;
import net.gogroups.security.model.ApiSecurityClientUser;
import net.gogroups.security.model.ApiSecurityUser;
import net.gogroups.security.service.ApiSecurityService;
import net.gogroups.gowaka.constant.UserRoles;
import net.gogroups.gowaka.exception.ApiException;
import net.gogroups.gowaka.exception.ErrorCodes;
import net.gogroups.gowaka.service.OfficialAgencyService;
import net.gogroups.gowaka.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.gogroups.gowaka.constant.UserRoles.*;


/**
 * Author: Edward Tanko <br/>
 * Date: 9/17/19 8:23 PM <br/>
 */
@Service
public class OfficialAgencyServiceImpl implements OfficialAgencyService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private OfficialAgencyRepository officialAgencyRepository;
    private UserRepository userRepository;
    private UserService userService;
    private ApiSecurityService apiSecurityService;
    private ClientUserCredConfig clientUserCredConfig;

    @Autowired
    public OfficialAgencyServiceImpl(OfficialAgencyRepository officialAgencyRepository, UserRepository userRepository, UserService userService, ApiSecurityService apiSecurityService, ClientUserCredConfig clientUserCredConfig) {
        this.officialAgencyRepository = officialAgencyRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.apiSecurityService = apiSecurityService;
        this.clientUserCredConfig = clientUserCredConfig;
    }

    @Override
    public OfficialAgencyDTO createOfficialAgency(CreateOfficialAgencyDTO createOfficialAgencyDTO) {

        //make a call to get/verify the AgencyAdmin user
        // make a call to add AGENCY_ADMIN to user's role
        // add user to agency and save agency
        // create DTO and return

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();

        String username = createOfficialAgencyDTO.getAgencyAdminEmail();
        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUsername(username, clientToken.getToken());


        String userRole = apiSecurityUser.getRoles() + AGENCY_ADMIN.toString();
        apiSecurityService.updateUserInfo(apiSecurityUser.getId(), "ROLES", userRole, clientToken.getToken());

        Optional<User> userOptional = userRepository.findById(apiSecurityUser.getId());
        if (!userOptional.isPresent()) {
            throw new ApiException("Agency admin user not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userOptional.get();

        if (user.getOfficialAgency() != null) {
            throw new ApiException("User already a member of an agency.", ErrorCodes.USER_ALREADY_IN_AN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

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

    @Override
    public OfficialAgencyUserDTO assignAgencyUserRole(OfficialAgencyUserRoleRequestDTO officialAgencyUserRoleRequestDTO) {

        String userId = officialAgencyUserRoleRequestDTO.getUserId();

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userOptional.get();

        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        Optional<User> currentAuthUserOptional = userRepository.findById(currentAuthUser.getId());
        if (!currentAuthUserOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User authUser = currentAuthUserOptional.get();

        if (!user.getOfficialAgency().getId().equals(authUser.getOfficialAgency().getId())) {
            throw new ApiException("User must be a member to your agency.", ErrorCodes.USER_NOT_IN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        List<UserRoles> systemRoles = Collections.arrayToList(UserRoles.values());

        List<String> roles = officialAgencyUserRoleRequestDTO.getRoles().stream()
                .filter(role -> systemRoles.contains(UserRoles.valueOf(role)))
                .filter(role->!role.equalsIgnoreCase(AGENCY_ADMIN.toString()))
                .filter(role->!role.equalsIgnoreCase(GW_ADMIN.toString()))
                .collect(Collectors.toList());
        String userRole = USERS.toString();
        for (String role : roles) {
            userRole += ";" + role;
        }

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();
        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUserId(userId, clientToken.getToken());

        apiSecurityService.updateUserInfo(apiSecurityUser.getId(), "ROLES", userRole, clientToken.getToken());
        OfficialAgencyUserDTO officialAgencyUserDTO = new OfficialAgencyUserDTO();
        officialAgencyUserDTO.setFullName(apiSecurityUser.getFullName());
        officialAgencyUserDTO.setId(apiSecurityUser.getId());
        officialAgencyUserDTO.setRoles(Collections.arrayToList(userRole.split(";")));

        return officialAgencyUserDTO;
    }

    @Override
    public List<OfficialAgencyUserDTO> getAgencyUsers() {

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();
        UserDTO currentAuthUser = userService.getCurrentAuthUser();

        Optional<User> userOptional = userRepository.findById(currentAuthUser.getId());
        if (!userOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User authUser = userOptional.get();
        OfficialAgency officialAgency = authUser.getOfficialAgency();
        if (officialAgency == null) {
            throw new ApiException("User Agency not found.", ErrorCodes.USER_NOT_IN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        List<User> agencyUsers = officialAgency.getUsers();

        return agencyUsers.stream()
                .filter(user -> user.getUserId() != authUser.getUserId())
                .map(user -> {
                    try {
                        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUserId(user.getUserId(), clientToken.getToken());
                        OfficialAgencyUserDTO officialAgencyUserDTO = new OfficialAgencyUserDTO();
                        officialAgencyUserDTO.setId(apiSecurityUser.getId());
                        officialAgencyUserDTO.setFullName(apiSecurityUser.getFullName());
                        officialAgencyUserDTO.setRoles(Collections.arrayToList(apiSecurityUser.getRoles().split(";")));
                        return officialAgencyUserDTO;
                    } catch (Exception ex) {
                        logger.info("User <{}> data not in sync with ApiSecurity: {}", user.getUserId(), ex.getMessage());
                    }
                    return new OfficialAgencyUserDTO();
                })
                .filter(officialAgencyUserDTO -> officialAgencyUserDTO.getId() != null)
                .collect(Collectors.toList());
    }

    @Override
    public OfficialAgencyUserDTO addAgencyUser(EmailDTO emailDTO) {

        UserDTO authUserDTO = userService.getCurrentAuthUser();
        Optional<User> authUserOptional = userRepository.findById(authUserDTO.getId());
        if(!authUserOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ApiSecurityAccessToken apiSecurityAccessToken = getApiSecurityAccessToken();
        ApiSecurityUser apiSecurityUser = apiSecurityService.getUserByUsername(emailDTO.getEmail(), apiSecurityAccessToken.getToken());

        Optional<User> userOptional = userRepository.findById(apiSecurityUser.getId());
        if(!userOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User authUser = authUserOptional.get();
        User user = userOptional.get();
        if(user.getOfficialAgency()!=null){
            throw new ApiException("User already a member of an agency.", ErrorCodes.USER_ALREADY_IN_AN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        user.setOfficialAgency(authUser.getOfficialAgency());
        userRepository.save(user);

        OfficialAgencyUserDTO officialAgencyUserDTO = new OfficialAgencyUserDTO();
        officialAgencyUserDTO.setFullName(apiSecurityUser.getFullName());
        officialAgencyUserDTO.setId(apiSecurityUser.getId());
        officialAgencyUserDTO.setRoles(Collections.arrayToList(apiSecurityUser.getRoles().split(";")));

        return officialAgencyUserDTO;
    }

    @Override
    public void removeAgencyUser(String userId) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        User user = userOptional.get();

        UserDTO currentAuthUser = userService.getCurrentAuthUser();
        Optional<User> currentAuthUserOptional = userRepository.findById(currentAuthUser.getId());
        if (!currentAuthUserOptional.isPresent()) {
            throw new ApiException("User not found.", ErrorCodes.RESOURCE_NOT_FOUND.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User authUser = currentAuthUserOptional.get();
        if (!user.getOfficialAgency().getId().equals(authUser.getOfficialAgency().getId())) {
            throw new ApiException("User must be a member to your agency.", ErrorCodes.USER_NOT_IN_AGENCY.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(user.getUserId().equals(authUser.getUserId())){
            throw new ApiException("Operation not allowed.", ErrorCodes.VALIDATION_ERROR.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();
        apiSecurityService.updateUserInfo(userId, "ROLES", USERS.toString(), clientToken.getToken());

        user.setOfficialAgency(null);
        userRepository.save(user);

    }

    private ApiSecurityAccessToken getApiSecurityAccessToken() {
        ApiSecurityClientUser apiSecurityClientUser = new ApiSecurityClientUser();
        apiSecurityClientUser.setClientId(clientUserCredConfig.getClientId());
        apiSecurityClientUser.setClientSecret(clientUserCredConfig.getClientSecret());
        return apiSecurityService.getClientToken(apiSecurityClientUser);
    }

}