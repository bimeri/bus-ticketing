package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 8:41 PM <br/>
 */
public interface UserService {

    UserDTO createUser(CreateUserRequest createUserRequest);
    TokenDTO loginUser(EmailPasswordDTO emailPasswordDTO);
    void changeUserPassword(ChangePasswordDTO changePasswordDTO);
    void forgotUserPassword(EmailDTO emailDTO);
    UserDTO getCurrentAuthUser();
    TokenDTO getNewToken(RefreshTokenDTO refreshTokenDTO);
}
