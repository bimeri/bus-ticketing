package net.gogroups.gowaka.service;

import net.gogroups.gowaka.dto.*;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 8:41 PM <br/>
 */
public interface UserService {

    UserDTO createUser(CreateUserRequest createUserRequest);

    TokenDTO loginUser(EmailPasswordDTO emailPasswordDTO, String sourceSystem);

    void changeUserPassword(ChangePasswordDTO changePasswordDTO);

    void forgotUserPassword(EmailDTO emailDTO);

    UserDTO getCurrentAuthUser();

    TokenDTO getNewToken(RefreshTokenDTO refreshTokenDTO);

    void updateProfile(UpdateProfileDTO updateProfileDTO);

    void verifyEmail(EmailDTO emailDTO);

    GWUserDTO validateGWUserByEmail(EmailDTO emailDTO);

    GWAccountDTO getAccountInfo(String code);
}
