package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
    @PostMapping("/public/get_token")
    ResponseEntity<TokenDTO> getNewToken(@RequestBody RefreshTokenDTO refreshTokenDTO){
        return ResponseEntity.ok(userService.getNewToken(refreshTokenDTO));
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

    @PreAuthorize("hasAnyRole('ROLE_USERS')")
    @PostMapping("/protected/users/profile")
    ResponseEntity<?> updateProfile(@RequestBody @Valid UpdateProfileDTO updateProfileDTO){
        userService.updateProfile(updateProfileDTO);
        return ResponseEntity.noContent().build();
    }

}
