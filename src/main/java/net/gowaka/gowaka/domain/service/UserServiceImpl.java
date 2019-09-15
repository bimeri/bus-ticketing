package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.GlobalConstants;
import net.gowaka.gowaka.domain.config.ClientUserCredConfig;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.CreateUserRequest;
import net.gowaka.gowaka.dto.UserDTO;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityAccessToken;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityClientUser;
import net.gowaka.gowaka.network.api.apisecurity.model.ApiSecurityUser;
import net.gowaka.gowaka.service.ApiSecurityService;
import net.gowaka.gowaka.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 8:41 PM <br/>
 */
@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private ApiSecurityService apiSecurityService;
    private ClientUserCredConfig clientUserCredConfig;

    public UserServiceImpl(UserRepository userRepository, ApiSecurityService apiSecurityService, ClientUserCredConfig clientUserCredConfig) {
        this.userRepository = userRepository;
        this.apiSecurityService = apiSecurityService;
        this.clientUserCredConfig = clientUserCredConfig;
    }

    @Override
    public UserDTO createUser(CreateUserRequest createUserRequest){

        ApiSecurityClientUser apiSecurityClientUser = new ApiSecurityClientUser();
        apiSecurityClientUser.setClientId(clientUserCredConfig.getClientId());
        apiSecurityClientUser.setClientSecret(clientUserCredConfig.getClientSecret());

        ApiSecurityAccessToken clientToken = apiSecurityService.getClientToken(apiSecurityClientUser);

        String roles = GlobalConstants.USERS;

        ApiSecurityUser apiSecurityUser = new ApiSecurityUser();
        apiSecurityUser.setFullName(createUserRequest.getFullName());
        apiSecurityUser.setUsername(createUserRequest.getEmail());
        apiSecurityUser.setEmail(createUserRequest.getEmail());
        apiSecurityUser.setPassword(createUserRequest.getPassword());
        apiSecurityUser.setRoles(roles);

        ApiSecurityUser savedApiSecurityUser = apiSecurityService.registerUser(apiSecurityUser, clientToken.getToken());

        User user = new User();
        user.setUserId(savedApiSecurityUser.getId());
        user.setTimestamp(LocalDateTime.now());
        userRepository.save(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getUserId());
        userDTO.setFullName(createUserRequest.getFullName());
        userDTO.setEmail(createUserRequest.getEmail());
        userDTO.setRoles(Arrays.asList(roles));

        return userDTO;
    }

}
