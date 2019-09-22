package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.domain.config.ClientUserCredConfig;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.network.api.apisecurity.model.*;
import net.gowaka.gowaka.security.UserDetailsImpl;
import net.gowaka.gowaka.service.ApiSecurityService;
import net.gowaka.gowaka.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.gowaka.gowaka.constant.UserRoles.USERS;

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

        ApiSecurityAccessToken clientToken = getApiSecurityAccessToken();

        String roles = USERS.toString();

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

    @Override
    public TokenDTO loginUser(EmailPasswordDTO emailPasswordDTO) {

        ApiSecurityUsernamePassword apiSecurityUsernamePassword = new ApiSecurityUsernamePassword();
        apiSecurityUsernamePassword.setUsername(emailPasswordDTO.getEmail());
        apiSecurityUsernamePassword.setPassword(emailPasswordDTO.getPassword());
        apiSecurityUsernamePassword.setAppName(clientUserCredConfig.getAppName());

        ApiSecurityAccessToken userToken = apiSecurityService.getUserToken(apiSecurityUsernamePassword);
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setHeader(userToken.getHeader());
        tokenDTO.setIssuer(userToken.getIssuer());
        tokenDTO.setType(userToken.getType());
        tokenDTO.setAccessToken(userToken.getToken());

        return tokenDTO;
    }

    @Override
    public void changeUserPassword(ChangePasswordDTO changePasswordDTO) {

        ApiSecurityChangePassword apiSecurityChangePassword = new ApiSecurityChangePassword();
        apiSecurityChangePassword.setUsername(changePasswordDTO.getEmail());
        apiSecurityChangePassword.setOldPassword(changePasswordDTO.getOldPassword());
        apiSecurityChangePassword.setPassword(changePasswordDTO.getPassword());

        apiSecurityService.changePassword(apiSecurityChangePassword);

    }

    @Override
    public void forgotUserPassword(EmailDTO emailDTO) {

        ApiSecurityForgotPassword apiSecurityForgotPassword = new ApiSecurityForgotPassword();
        apiSecurityForgotPassword.setApplicationName(clientUserCredConfig.getAppName());
        apiSecurityForgotPassword.setUsername(emailDTO.getEmail());
        apiSecurityService.forgotPassword(apiSecurityForgotPassword);
    }

    @Override
    public UserDTO getCurrentAuthUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userDetails.getId());
        userDTO.setFullName(userDetails.getFullName());
        userDTO.setEmail(userDetails.getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());
        userDTO.setRoles(roles);
        return userDTO;
    }

    private ApiSecurityAccessToken getApiSecurityAccessToken() {
        ApiSecurityClientUser apiSecurityClientUser = new ApiSecurityClientUser();
        apiSecurityClientUser.setClientId(clientUserCredConfig.getClientId());
        apiSecurityClientUser.setClientSecret(clientUserCredConfig.getClientSecret());

        return apiSecurityService.getClientToken(apiSecurityClientUser);
    }

}
