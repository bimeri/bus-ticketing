package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.domain.repository.UserRepository;
import net.gogroups.gowaka.dto.*;
import net.gogroups.notification.model.EmailAddress;
import net.gogroups.notification.model.SendEmailDTO;
import net.gogroups.notification.service.NotificationService;
import net.gogroups.security.accessconfig.JwtTokenProvider;
import net.gogroups.security.accessconfig.UserDetailsImpl;
import net.gogroups.security.model.*;
import net.gogroups.security.service.ApiSecurityService;
import net.gogroups.gowaka.constant.notification.EmailFields;
import net.gogroups.gowaka.domain.config.ClientUserCredConfig;
import net.gogroups.gowaka.domain.model.User;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.gogroups.gowaka.constant.UserRoles.USERS;

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
        user.setEmail(savedApiSecurityUser.getEmail());
        user.setFullName(savedApiSecurityUser.getFullName());
        userRepository.save(user);

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken(savedApiSecurityUser.getToken().getToken());
        tokenDTO.setExpiredIn(savedApiSecurityUser.getToken().getExpiredIn());
        tokenDTO.setRefreshToken(savedApiSecurityUser.getToken().getRefreshToken());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getUserId());
        userDTO.setFullName(createUserRequest.getFullName());
        userDTO.setEmail(createUserRequest.getEmail());
        userDTO.setRoles(Collections.singletonList(roles));
        userDTO.setToken(tokenDTO);

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

        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found.");
        }
        User user = userOptional.get();
        UserDTO userDTO = getUserDTO(userDetails, user);

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
    public void updateProfile(UpdateProfileDTO updateProfileDTO) {
        UserDTO currentAuthUser = getCurrentAuthUser();
        Optional<User> userOptional = userRepository.findById(currentAuthUser.getId());
        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("User not found.");
        }
        User user = userOptional.get();
        if (!currentAuthUser.getFullName().equals(updateProfileDTO.getFullName())) {
            ApiSecurityAccessToken apiSecurityAccessToken = getApiSecurityAccessToken();
            apiSecurityService.updateUserInfo(user.getUserId(), "FULL_NAME", updateProfileDTO.getFullName(), apiSecurityAccessToken.getToken());
            user.setFullName(updateProfileDTO.getFullName());
        }
        user.setIdCardNumber(updateProfileDTO.getIdCardNumber());
        user.setPhoneNumber(updateProfileDTO.getPhoneNumber());
        userRepository.save(user);
    }

    @Override
    public void verifyEmail(EmailDTO emailDTO) {

        ApiSecurityVerifyEmail apiSecurityVerifyEmail = new ApiSecurityVerifyEmail();
        apiSecurityVerifyEmail.setUsername(emailDTO.getEmail());
        apiSecurityVerifyEmail.setApplicationName(clientUserCredConfig.getAppName());
        apiSecurityService.verifyEmail(apiSecurityVerifyEmail);
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

    private UserDTO getUserDTO(UserDetailsImpl userDetails, User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userDetails.getId());
        userDTO.setFullName(userDetails.getFullName());
        userDTO.setEmail(userDetails.getUsername());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setIdCardNumber(user.getIdCardNumber());
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
