package net.gogroups.gowaka.controller;

import net.gogroups.gowaka.dto.*;
import net.gogroups.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<TokenDTO> loginUser(@RequestBody EmailPasswordDTO emailPasswordDTO, @RequestHeader(value = "X-Source-System", defaultValue = "WEB") String sourceSystem){
        return ResponseEntity.ok(userService.loginUser(emailPasswordDTO, sourceSystem));
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
    ResponseEntity<?> updateProfile(@RequestBody @Validated UpdateProfileDTO updateProfileDTO){
        userService.updateProfile(updateProfileDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/public/users/verify_email")
    ResponseEntity<?> verifyEmail(@RequestBody @Validated EmailDTO emailDTO){
        userService.verifyEmail(emailDTO);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/protected/users/validate_user")
    @PreAuthorize("hasAnyRole('ROLE_GW_ADMIN','ROLE_AGENCY_ADMIN','ROLE_AGENCY_MANAGER','ROLE_AGENCY_OPERATOR','ROLE_AGENCY_BOOKING', 'ROLE_AGENCY_CHECKING')")
    ResponseEntity<GWUserDTO> validateGWUserByEmail(@RequestBody @Validated EmailDTO emailDTO){
        return ResponseEntity.ok(userService.validateGWUserByEmail(emailDTO));
    }

    @PostMapping("/protected/users/account")
    @PreAuthorize("hasAnyRole('ROLE_GW_ADMIN','ROLE_AGENCY_ADMIN','ROLE_AGENCY_MANAGER','ROLE_AGENCY_OPERATOR','ROLE_AGENCY_BOOKING')")
    ResponseEntity<GWAccountDTO> getUserAccountInfo(@RequestBody @Validated CodeDTO codeDTO){
        return ResponseEntity.ok(userService.getAccountInfo(codeDTO.getCode()));
    }

}
