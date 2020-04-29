package net.gowaka.gowaka.domain.service;

import net.gowaka.gowaka.constant.notification.EmailFields;
import net.gowaka.gowaka.domain.config.ClientUserCredConfig;
import net.gowaka.gowaka.domain.model.User;
import net.gowaka.gowaka.domain.repository.UserRepository;
import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.network.api.apisecurity.model.*;
import net.gowaka.gowaka.network.api.notification.model.EmailAddress;
import net.gowaka.gowaka.network.api.notification.model.SendEmailDTO;
import net.gowaka.gowaka.security.JwtTokenProvider;
import net.gowaka.gowaka.security.UserDetailsImpl;
import net.gowaka.gowaka.service.ApiSecurityService;
import net.gowaka.gowaka.service.NotificationService;
import net.gowaka.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
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
    private JwtTokenProvider jwtTokenProvider;
    private NotificationService notificationService;
    private EmailContentBuilder emailContentBuilder;


    public UserServiceImpl(UserRepository userRepository, ApiSecurityService apiSecurityService,
                           ClientUserCredConfig clientUserCredConfig, JwtTokenProvider jwtTokenProvider,
                           EmailContentBuilder emailContentBuilder) {
        this.userRepository = userRepository;
        this.apiSecurityService = apiSecurityService;
        this.clientUserCredConfig = clientUserCredConfig;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailContentBuilder = emailContentBuilder;

    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public UserDTO createUser(CreateUserRequest createUserRequest) {

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
        userDTO.setRoles(Collections.singletonList(roles));

        sendWelcomeEmail(createUserRequest);

        return userDTO;
    }

    @Override
    public TokenDTO loginUser(EmailPasswordDTO emailPasswordDTO) {

        ApiSecurityUsernamePassword apiSecurityUsernamePassword = new ApiSecurityUsernamePassword();
        apiSecurityUsernamePassword.setUsername(emailPasswordDTO.getEmail());
        apiSecurityUsernamePassword.setPassword(emailPasswordDTO.getPassword());
        apiSecurityUsernamePassword.setAppName(clientUserCredConfig.getAppName());

        ApiSecurityAccessToken userToken = apiSecurityService.getUserToken(apiSecurityUsernamePassword);
        UserDetailsImpl userDetails = jwtTokenProvider.getUserDetails(userToken.getToken());
        UserDTO userDTO = getUserDTO(userDetails);

        TokenDTO tokenDTO = getTokenDTO(userToken);
        tokenDTO.setUserDetails(userDTO);
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
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        userDTO.setRoles(roles);
        return userDTO;
    }

    @Override
    public TokenDTO getNewToken(RefreshTokenDTO refreshTokenDTO) {
        ApiRefreshToken apiRefreshToken = new ApiRefreshToken();
        apiRefreshToken.setRefreshToken(refreshTokenDTO.getRefreshToken());
        ApiSecurityAccessToken userToken = apiSecurityService.getNewUserToken(apiRefreshToken);
        TokenDTO tokenDTO = getTokenDTO(userToken);
        tokenDTO.setUserDetails(null);
        return tokenDTO;
    }

    private ApiSecurityAccessToken getApiSecurityAccessToken() {
        ApiSecurityClientUser apiSecurityClientUser = new ApiSecurityClientUser();
        apiSecurityClientUser.setClientId(clientUserCredConfig.getClientId());
        apiSecurityClientUser.setClientSecret(clientUserCredConfig.getClientSecret());

        return apiSecurityService.getClientToken(apiSecurityClientUser);
    }

    private UserDTO getUserDTO(UserDetailsImpl userDetails) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userDetails.getId());
        userDTO.setFullName(userDetails.getFullName());
        userDTO.setEmail(userDetails.getUsername());
        userDTO.setRoles(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );
        return userDTO;
    }

    private void sendWelcomeEmail(CreateUserRequest createUserRequest) {
        // send welcome email
        String message = emailContentBuilder.buildWelcomeEmail(createUserRequest.getFullName(), createUserRequest.getEmail(),
                clientUserCredConfig.getUrl(), LocalDate.now().getYear());
        SendEmailDTO emailDTO = new SendEmailDTO();
        emailDTO.setSubject(EmailFields.WELCOME_SUBJECT.getMessage());
        emailDTO.setMessage(message);

        emailDTO.setToAddresses(Collections.singletonList(new EmailAddress(
                createUserRequest.getEmail(),
                createUserRequest.getFullName()
        )));
        // setting cc and bcc to empty lists
        emailDTO.setCcAddresses(Collections.emptyList());
        emailDTO.setBccAddresses(Collections.emptyList());
        notificationService.sendEmail(emailDTO);
    }

    private TokenDTO getTokenDTO(ApiSecurityAccessToken userToken) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setHeader(userToken.getHeader());
        tokenDTO.setIssuer(userToken.getIssuer());
        tokenDTO.setType(userToken.getType());
        tokenDTO.setAccessToken(userToken.getToken());
        tokenDTO.setRefreshToken(userToken.getRefreshToken());
        tokenDTO.setExpiredIn(userToken.getExpiredIn());
        return tokenDTO;
    }

}
