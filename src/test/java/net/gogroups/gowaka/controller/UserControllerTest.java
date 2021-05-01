package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/14/19 10:25 AM <br/>
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService mockUserService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(mockUserService);
    }

    @Test
    void createUser_calls_UserService() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        userController.createUser(createUserRequest);
        verify(mockUserService).createUser(createUserRequest);
    }

    @Test
    void loginUser_calls_UserService() {
        EmailPasswordDTO emailPasswordDTO = new EmailPasswordDTO();
        userController.loginUser(emailPasswordDTO, "MOBILE");
        verify(mockUserService).loginUser(emailPasswordDTO, "MOBILE");
    }

    @Test
    void getNewToken_calls_UserService() {
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO();
        userController.getNewToken(refreshTokenDTO);
        verify(mockUserService).getNewToken(refreshTokenDTO);
    }

    @Test
    void changeUserPassword_calls_UserService() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        userController.changeUserPassword(changePasswordDTO);
        verify(mockUserService).changeUserPassword(changePasswordDTO);
    }
    @Test
    void forgotUserPassword_calls_UserService() {
        EmailDTO emailDTO = new EmailDTO();
        userController.forgotUserPassword(emailDTO);
        verify(mockUserService).forgotUserPassword(emailDTO);
    }

    @Test
    void updateProfile_calls_UserService() {
        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        userController.updateProfile(updateProfileDTO);
        verify(mockUserService).updateProfile(updateProfileDTO);
    }


    @Test
    void verifyEmail_calls_UserService() {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("email@email.com");
        ResponseEntity<?> response = userController.verifyEmail(emailDTO);
        verify(mockUserService).verifyEmail(emailDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void validateGWUserByEmail_calls_UserService() {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail("email@email.com");
        userController.validateGWUserByEmail(emailDTO);
        verify(mockUserService).validateGWUserByEmail(emailDTO);
    }

 @Test
    void getUserAccountInfo_calls_UserService() {
        CodeDTO codeDTO = new CodeDTO();
        codeDTO.setCode("1234");
        userController.getUserAccountInfo(codeDTO);
        verify(mockUserService).getAccountInfo("1234");
    }

}
