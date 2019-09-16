package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/14/19 10:14 AM <br/>
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/public/register")
    ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest createUserRequest){
        return ResponseEntity.ok(userService.createUser(createUserRequest));
    }
    @PostMapping("/public/login")
    ResponseEntity<TokenDTO> loginUser(@RequestBody EmailPasswordDTO emailPasswordDTO){
        return ResponseEntity.ok(userService.loginUser(emailPasswordDTO));
    }
    @PostMapping("/public/change_password")
    ResponseEntity<?> changeUserPassword(@RequestBody ChangePasswordDTO changePasswordDTO){
        userService.changeUserPassword(changePasswordDTO);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/public/forgot_password")
    ResponseEntity<?> forgotUserPassword(@RequestBody EmailDTO emailDTO){
        userService.forgotUserPassword(emailDTO);
        return ResponseEntity.noContent().build();
    }

}
