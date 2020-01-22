package net.gowaka.gowaka.controller;

import net.gowaka.gowaka.dto.*;
import net.gowaka.gowaka.service.JourneyService;
import net.gowaka.gowaka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/14/19 10:14 AM <br/>
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private JourneyService journeyService;
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setJourneyService(JourneyService journeyService) {
        this.journeyService = journeyService;
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

    @GetMapping("/public/journey/search/departure/{departureLocationID}/destination/{destinationLocationID}")
    public ResponseEntity<List<JourneyResponseDTO>> searchJourney(@PathVariable ("departureLocationID") Long departureLocationId,
                                                                  @PathVariable ("destinationLocationID") Long destinationLocationId,
                                                                  @RequestParam ("time") String time)
    {

        return ResponseEntity.ok(journeyService.searchJourney(departureLocationId, destinationLocationId, time));
    }

}
