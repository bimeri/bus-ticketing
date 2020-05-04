package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.verify;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/14/19 10:25 AM <br/>
 */
@RunWith(SpringRunner.class)
public class UserControllerTest {

    @Mock
    private UserService mockUserService;

    private UserController userController;

    @Before
    public void setUp() {
        userController = new UserController(mockUserService);
    }

    @Test
    public void createUser_calls_UserService() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        userController.createUser(createUserRequest);
        verify(mockUserService).createUser(createUserRequest);
    }

    @Test
    public void loginUser_calls_UserService() {
        EmailPasswordDTO emailPasswordDTO = new EmailPasswordDTO();
        userController.loginUser(emailPasswordDTO);
        verify(mockUserService).loginUser(emailPasswordDTO);
    }

    @Test
    public void getNewToken_calls_UserService() {
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO();
        userController.getNewToken(refreshTokenDTO);
        verify(mockUserService).getNewToken(refreshTokenDTO);
    }

    @Test
    public void changeUserPassword_calls_UserService() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        userController.changeUserPassword(changePasswordDTO);
        verify(mockUserService).changeUserPassword(changePasswordDTO);
    }
    @Test
    public void forgotUserPassword_calls_UserService() {
        EmailDTO emailDTO = new EmailDTO();
        userController.forgotUserPassword(emailDTO);
        verify(mockUserService).forgotUserPassword(emailDTO);
    }

}
