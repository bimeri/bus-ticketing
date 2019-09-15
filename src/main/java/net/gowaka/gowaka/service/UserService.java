package net.gowaka.gowaka.service;

import net.gowaka.gowaka.dto.CreateUserRequest;
import net.gowaka.gowaka.dto.EmailPasswordDTO;
import net.gowaka.gowaka.dto.TokenDTO;
import net.gowaka.gowaka.dto.UserDTO;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/13/19 8:41 PM <br/>
 */
public interface UserService {

    UserDTO createUser(CreateUserRequest createUserRequest);
    TokenDTO loginUser(EmailPasswordDTO emailPasswordDTO);
}
